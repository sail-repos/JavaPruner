package Reducer;

import JimpleMixer.blocks.*;
import Utils.ClassUtil;
import dtjvms.executor.JIT.JvmOutput;
import soot.*;
import soot.coffi.CFG;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.internal.*;

import java.util.*;

import static Utils.FileUtil.copyFile;

public class BlockReducer extends Reducer{

    private SootClass sootClass;
    private List<BlockInfo> blockInfos = new ArrayList<>();  // Block列表

    private Map<Integer, BlockStruct> structMap = new HashMap<>();  // 结构集合
    private final Set<Integer> handledNode = new HashSet<>();  // 已处理节点集合
    private final Set<Unit> reducedUnit = new HashSet<>();  // 已删除的unit集合

    private StructParser structParser; // 结构解析器

    private HashMap<String, JvmOutput> originOutput;
    private ArrayList<String> pOtions;

    private int testtype;

    private Set<String> handledMethod = new HashSet<>();  // 已处理的methods

    public BlockReducer(){
        super();
    }

    public BlockReducer(Checker checker, String classPath){
        super(checker, classPath);
    }

    @Override
    public boolean doReduce(SootClass sootClass, HashMap<String, JvmOutput> originOutput, ArrayList<String> pOtions, int testtype){
        this.sootClass = sootClass;
        this.originOutput = originOutput;
        this.pOtions = pOtions;
        this.testtype = testtype;
        for(SootMethod method : this.sootClass.getMethods()){
            if(!method.isConstructor() /*&& !method.getName().equals("<clinit>")*/){
                if(handledMethod.contains(method.getSubSignature())){
                    continue;
                }
                blockInfos = MyBlockContainer.getBlockFromMethod(sootClass, method);
                pretreatment(method.retrieveActiveBody());
                reduceInMethod(method);
                handledMethod.add(method.getSubSignature());
            }
        }
//        ClassUtil.saveClass(sootClass, "sootOutput", this.sootClass.getShortName());
        ClassUtil.saveClass(sootClass, checker.getDir(), this.sootClass.getShortName());

        return checker.bugChecker(originOutput, sootClass.getName(), pOtions, new String[1], testtype);
    }

    /**
     * 预处理block
     */
    public void pretreatment(Body body){
        structParser = new CFGStructParser(blockInfos, body);
        structParser.parse();
        structParser.handleStructs();
//        structParser.handleBasicStruct(structParser.structList);
        structMap = structParser.structMap;
    }

    public void reduceInMethod(SootMethod sootMethod){
        handledNode.clear();
        reducedUnit.clear();
        Body body = sootMethod.retrieveActiveBody();
        UnitPatchingChain units = body.getUnits();
        if(sootMethod.getReturnType() instanceof VoidType && (!(units.getLast() instanceof JReturnVoidStmt) || !(units.getLast() instanceof ReturnStmt))){
            JReturnVoidStmt jReturnVoidStmt = new JReturnVoidStmt();
            units.addLast(jReturnVoidStmt);
        }
        // 从内到外约减
        List<BlockStruct> blockStructs = new ArrayList<>();
        for(BlockStruct blockStruct : structParser.structList){
            getReduceOrder(blockStructs, blockStruct);
        }
        Collections.reverse(blockStructs);
        for(BlockStruct blockStruct : blockStructs){
            reduceInStruct(body, blockStruct);
        }

        if(!checker.bugChecker(originOutput, sootClass.getName(), pOtions, new String[1], testtype)){
            System.out.println("reload");
            String fullName = sootClass.getName();
            copyFile(ReduceChain.tmp, checker.getDir(), sootClass.getShortName() + ".class", sootClass.getShortName() + ".class");
            G.reset();
            ClassUtil.initSootEnvWithClassPath(ReduceChain.executePath);
            sootClass = ClassUtil.loadClass(fullName);
        }else {
            ClassUtil.saveClass(sootClass, ReduceChain.tmp, sootClass.getShortName());
        }
    }

    private void getReduceOrder(List<BlockStruct> blockStructs, BlockStruct blockStruct){
        blockStructs.add(blockStruct);
        for(BlockStruct sub : blockStruct.subStruct){
            getReduceOrder(blockStructs, sub);
        }
    }

    public void reduceInStruct(Body body, BlockStruct blockStruct){
        System.out.println(blockStruct.id);
        if(!blockStruct.isBasic && !StructParser.isStructEmpty(blockStruct)){
            return;
        }
        UnitPatchingChain units = body.getUnits();
        List<Unit> unitList = blockStruct.unitList;
        // 获取前置节点
        Unit preUnit = null;
        for(Unit u : unitList){
            if(units.contains(u)){
                preUnit = units.getPredOf(u);
                break;
            }
        }

        Map<Unit, Set<Unit>> targetUnitMap = new HashMap<>();
        for(Unit unit : units){
            if(unit instanceof IfStmt){
                Unit tar = ((IfStmt) unit).getTarget();
                Set<Unit> set = targetUnitMap.getOrDefault(tar, new HashSet<>());
                set.add(unit);
                targetUnitMap.put(tar, set);
            }else if(unit instanceof GotoStmt){
                Unit tar = ((GotoStmt) unit).getTarget();
                Set<Unit> set = targetUnitMap.getOrDefault(tar, new HashSet<>());
                set.add(unit);
                targetUnitMap.put(tar, set);
            }
        }
        int type = blockStruct.type;
        boolean isDelete = false;
        if(type == StructParser.TRAP){
            isDelete = reduceTrap(preUnit, unitList, body, targetUnitMap);
        }else if(type == StructParser.STMT){
            isDelete = reduceStmt(preUnit, unitList,body, targetUnitMap);
        }else if(type == StructParser.IF){
            isDelete = reduceIf(preUnit, unitList, body, targetUnitMap);
        }else if(type == StructParser.SWITCH){
            isDelete = reduceSwitch(preUnit, unitList, body, targetUnitMap);
        }else {
            isDelete = reduceLoop(preUnit, unitList, body, targetUnitMap, blockStruct.loopIndexes);
        }

        blockStruct.isDelete = isDelete;
    }

    private boolean unitGeneralJudge(Unit unit, List<Unit> reducedTmp){
        return !unit.toString().contains(":= @p") && !(unit instanceof ReturnStmt) && !(unit instanceof JReturnVoidStmt) && !structParser.isDepended(unit, reducedTmp, reducedUnit);
    }

    private boolean reduceTrap(Unit preUnit, List<Unit> unitList, Body body, Map<Unit, Set<Unit>> targetUnitMap){
        UnitPatchingChain units = body.getUnits();
        List<Unit> reducedTmp = new ArrayList<>();

        Map<Unit, Set<Trap>> trapBeginMap = new HashMap<>();
        Map<Unit, Set<Trap>> trapEndMap = new HashMap<>();
        Map<Unit, Set<Trap>> trapHandleMap = new HashMap<>();
        for(Trap trap : body.getTraps()){
            Unit begin = trap.getBeginUnit();
            Unit end = trap.getEndUnit();
            Unit handle = trap.getHandlerUnit();
            Set<Trap> begins = trapBeginMap.getOrDefault(begin, new HashSet<>());
            Set<Trap> ends = trapEndMap.getOrDefault(end, new HashSet<>());
            Set<Trap> handles = trapHandleMap.getOrDefault(handle, new HashSet<>());
            begins.add(trap);
            ends.add(trap);
            handles.add(trap);
            trapBeginMap.put(begin, begins);
            trapEndMap.put(end, ends);
            trapHandleMap.put(handle, handles);
        }

        for(int idx = unitList.size() - 1; idx > -1; idx--){
            Unit unit = unitList.get(idx);
            if(unitGeneralJudge(unit, reducedTmp) && !structParser.globalLoopIndexes.contains(unit)){
                units.remove(unit);
                reducedTmp.add(unit);
            }
        }

        ClassUtil.saveClass(sootClass, checker.getDir(), this.sootClass.getShortName());
        if(!checker.bugChecker(originOutput, sootClass.getName(), pOtions, new String[1], testtype)){
            restore(preUnit, unitList, units, targetUnitMap);
            for(Unit unit : unitList){
                for(Trap trap : trapBeginMap.getOrDefault(unit, new HashSet<>())){
                    trap.setBeginUnit(unit);
                }
                for(Trap trap : trapEndMap.getOrDefault(unit, new HashSet<>())){
                    trap.setEndUnit(unit);
                }
                for(Trap trap : trapHandleMap.getOrDefault(unit, new HashSet<>())){
                    trap.setHandlerUnit(unit);
                }
            }
            ClassUtil.saveClass(sootClass, checker.getDir(), this.sootClass.getShortName());
            return false;
        }else {
            reducedUnit.addAll(reducedTmp);
            ClassUtil.saveClass(sootClass, ReduceChain.tmp, sootClass.getShortName());
            return true;
        }
    }

    private boolean reduceIf(Unit preUnit, List<Unit> unitList, Body body, Map<Unit, Set<Unit>> targetUnitMap){
        UnitPatchingChain units = body.getUnits();
        List<Unit> reducedTmp = new ArrayList<>();
        for(int idx = unitList.size() - 1; idx > -1; idx--){
            Unit unit = unitList.get(idx);
            if(unitGeneralJudge(unit, reducedTmp) && !structParser.globalLoopIndexes.contains(unit)){
                units.remove(unit);
                reducedTmp.add(unit);
            }
        }
        ClassUtil.saveClass(sootClass, checker.getDir(), this.sootClass.getShortName());
        if(!checker.bugChecker(originOutput, sootClass.getName(), pOtions, new String[1], testtype)){
            restore(preUnit, unitList, units, targetUnitMap);
            ClassUtil.saveClass(sootClass, checker.getDir(), this.sootClass.getShortName());
            return false;
        }else {
            reducedUnit.addAll(reducedTmp);
            ClassUtil.saveClass(sootClass, ReduceChain.tmp, sootClass.getShortName());
            return true;
        }
    }

    private boolean reduceLoop(Unit preUnit, List<Unit> unitList, Body body, Map<Unit, Set<Unit>> targetUnitMap, Set<Unit> indexesSet){
        UnitPatchingChain units = body.getUnits();
        List<Unit> reducedTmp = new ArrayList<>();
        for(int idx = unitList.size() - 1; idx > -1; idx--){
            Unit unit = unitList.get(idx);
            if(unitGeneralJudge(unit, reducedTmp) && (!structParser.globalLoopIndexes.contains(unit) || indexesSet.contains(unit))){
                units.remove(unit);
                reducedTmp.add(unit);
            }
        }
        ClassUtil.saveClass(sootClass, checker.getDir(), this.sootClass.getShortName());
        if(!checker.bugChecker(originOutput, sootClass.getName(), pOtions, new String[1], testtype)){
            restore(preUnit, unitList, units, targetUnitMap);
            ClassUtil.saveClass(sootClass, checker.getDir(), this.sootClass.getShortName());
            return false;
        }else {
            reducedUnit.addAll(reducedTmp);
            ClassUtil.saveClass(sootClass, ReduceChain.tmp, sootClass.getShortName());
            return true;
        }
    }

    private boolean reduceSwitch(Unit preUnit, List<Unit> unitList, Body body, Map<Unit, Set<Unit>> targetUnitMap){
        UnitPatchingChain units = body.getUnits();
        List<Unit> reducedTmp = new ArrayList<>();
        for(int idx = unitList.size() - 1; idx > -1; idx--){
            Unit unit = unitList.get(idx);
            if(unitGeneralJudge(unit, reducedTmp) && !structParser.globalLoopIndexes.contains(unit)){
                units.remove(unit);
                reducedTmp.add(unit);
            }
        }

        ClassUtil.saveClass(sootClass, checker.getDir(), this.sootClass.getShortName());
        if(!checker.bugChecker(originOutput, sootClass.getName(), pOtions, new String[1], testtype)){
            restore(preUnit, unitList, units, targetUnitMap);
            ClassUtil.saveClass(sootClass, checker.getDir(), this.sootClass.getShortName());
            return false;
        }else {
            reducedUnit.addAll(reducedTmp);
            ClassUtil.saveClass(sootClass, ReduceChain.tmp, sootClass.getShortName());
            return true;
        }
    }

    private boolean reduceStmt(Unit preUnit, List<Unit> unitList, Body body, Map<Unit, Set<Unit>> targetUnitMap){
        UnitPatchingChain units = body.getUnits();
        List<Unit> reducedTmp = new ArrayList<>();
        Set<Unit> branchSet = structParser.branchSet;
        for(int idx = unitList.size() - 1; idx > -1; idx--){
            Unit unit = unitList.get(idx);
            if(unitGeneralJudge(unit, reducedTmp) && !branchSet.contains(unit) && !structParser.globalLoopIndexes.contains(unit) && !unit.toString().contains(":= @caughtexception")){
                units.remove(unit);
                reducedTmp.add(unit);
            }
        }

        ClassUtil.saveClass(sootClass, checker.getDir(), this.sootClass.getShortName());
        if(!checker.bugChecker(originOutput, sootClass.getName(), pOtions, new String[1], testtype)){
            restore(preUnit, unitList, units, targetUnitMap);
            ClassUtil.saveClass(sootClass, checker.getDir(), this.sootClass.getShortName());
            return false;
        }else {
            reducedUnit.addAll(reducedTmp);
            ClassUtil.saveClass(sootClass, ReduceChain.tmp, sootClass.getShortName());
            return true;
        }
    }

    public void restore(Unit preUnit, List<Unit> handledUnits, UnitPatchingChain units, Map<Unit, Set<Unit>> targetUnitMap){
//        Collections.reverse(reducedUnits);
        if(preUnit != null) {
            for(Unit unit : handledUnits){
                if(!reducedUnit.contains(unit)){
                    if(!units.contains(unit)) {
                        units.insertAfter(unit, preUnit);
                    }
                    preUnit = unit;
                }
            }
//            units.insertAfter(handledUnits, preUnit);
        }else {
            units.removeAll(handledUnits);
            for(int i = handledUnits.size() - 1; i > -1; i--){
                if(!reducedUnit.contains(handledUnits.get(i))) {
                    units.addFirst(handledUnits.get(i));
                }
            }
        }
        for(Unit unit : handledUnits){
            if(reducedUnit.contains(unit)){
                continue;
            }
            for(Unit src : targetUnitMap.getOrDefault(unit, new HashSet<>())){
                if(src instanceof JIfStmt){
                    ((JIfStmt) src).setTarget(unit);
                }else if(src instanceof JGotoStmt){
                    ((JGotoStmt) src).setTarget(unit);
                }
            }
        }
    }

}