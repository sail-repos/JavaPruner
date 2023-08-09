package Reducer;

import JimpleMixer.blocks.BlockInfo;
import JimpleMixer.blocks.MyBlockContainer;
import Provider.PrimitiveValueProvider;
import Utils.ClassUtil;
import dtjvms.executor.JIT.JvmOutput;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsertionReducer extends Reducer{

    private SootClass sootClass;

    private StructParser structParser;

    private final String magicStr = "K-ON";
    private final String linkStr = "-:-";

    private final Set<String> methodBlockSet;

    private final Set<String> blockRunSet;

    private final Map<String, List<BlockStruct>> methodBlockMap;

    private final Map<String, Map<Unit, Integer>> methodUnitIdxMap;

    private SootMethod currentMethod;
    private final Map<String, SootMethod> signatureMap;

    private final Set<Integer> reducedBlockSet;

    private final Map<BlockStruct, List<Unit>> block2PrintMap;
    private final Set<Unit> reducedUnits;

    private final Set<Unit> branchSet;

    private final Map<String, Map<Local, List<Unit>>> methodLocalDef;
    private final Map<String, Map<Local, List<Unit>>> methodLocalDependency;

    private int testtype;

    public InsertionReducer(){
        super();
        methodBlockSet = new HashSet<>();
        blockRunSet = new HashSet<>();
        methodBlockMap = new HashMap<>();
        signatureMap = new HashMap<>();
        reducedBlockSet = new HashSet<>();
        block2PrintMap = new HashMap<>();
        reducedUnits = new HashSet<>();
        methodUnitIdxMap = new HashMap<>();
        branchSet = new HashSet<>();
        methodLocalDef = new HashMap<>();
        methodLocalDependency = new HashMap<>();
    }

    public InsertionReducer(Checker checker, String classPath){
        super(checker, classPath);
        methodBlockSet = new HashSet<>();
        blockRunSet = new HashSet<>();
        methodBlockMap = new HashMap<>();
        signatureMap = new HashMap<>();
        reducedBlockSet = new HashSet<>();
        block2PrintMap = new HashMap<>();
        reducedUnits = new HashSet<>();
        methodUnitIdxMap = new HashMap<>();
        branchSet = new HashSet<>();
        methodLocalDef = new HashMap<>();
        methodLocalDependency = new HashMap<>();
    }

    @Override
    public boolean doReduce(SootClass sootClass, HashMap<String, JvmOutput> result, ArrayList<String> options, int testtype) {
        this.sootClass = sootClass;
        this.testtype = testtype;
        for(SootMethod method : sootClass.getMethods()){
            signatureMap.put(method.getSubSignature(), method);
        }

        insert();
        reduceGhostBlock(options);

//        ClassUtil.saveClass(sootClass, "sootOutput", sootClass.getShortName());
        ClassUtil.saveClass(sootClass, checker.getDir(),  sootClass.getShortName());

        return checker.bugChecker(result, sootClass.getName(), options, new String[1], testtype);
    }

    private String getMethodBlockPair(SootMethod sootMethod, BlockStruct blockStruct){
        return magicStr + sootMethod.getSubSignature() + linkStr + blockStruct.id;
    }

    private void insert(){
        for(SootMethod sootMethod : sootClass.getMethods()){
            if(sootMethod.isConstructor() || sootMethod.getName().equals("<clinit>")){
                continue;
            }
            List<BlockInfo> blockInfos = MyBlockContainer.getBlockFromMethod(sootClass, sootMethod);
            Body body = sootMethod.retrieveActiveBody();
            structParser = new GrammarStructParser(blockInfos, body);
            structParser.parse();
            branchSet.addAll(structParser.branchSet);
            methodLocalDependency.put(sootMethod.getSubSignature(), structParser.localDependencyMap);
            methodLocalDef.put(sootMethod.getSubSignature(), structParser.localDefMap);
            List<BlockStruct> blockStructs = new ArrayList<>();
            UnitPatchingChain units = body.getUnits();
            for(BlockStruct blockStruct : structParser.structList){
                blockStructs.add(blockStruct);
                String methodBlockPair = this.getMethodBlockPair(sootMethod, blockStruct);
                methodBlockSet.add(methodBlockPair);
                SootClass StmtInsertionClass = ClassUtil.loadClass("Reducer.StmtInsertion");
                SootMethod method = StmtInsertionClass.getMethod("void printBlock(java.lang.String)");
                InvokeStmt invokeCall = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(method.makeRef(), StringConstant.v(methodBlockPair)));
                Unit headUnit = blockStruct.unitList.get(0);
                while(headUnit instanceof IdentityStmt){
                    headUnit = units.getSuccOf(headUnit);
                }
                Unit pre = units.getPredOf(headUnit);
                if(pre == null){
                    units.addFirst(invokeCall);
                }else {
                    units.insertBefore(invokeCall, headUnit);
                }
                List<Unit> prints = new ArrayList<>();
                prints.add(invokeCall);
                block2PrintMap.put(blockStruct, prints);
            }

            methodBlockMap.put(sootMethod.getSubSignature(), blockStructs);
            methodUnitIdxMap.put(sootMethod.getSubSignature(), structParser.unitIdxMap);
        }

        ClassUtil.saveClass(sootClass, checker.getDir(), sootClass.getShortName());
    }


    /**
     * 删除不被执行的幽灵代码
     */
    private void reduceGhostBlock(ArrayList<String> options){
        Map<String, JvmOutput> result = checker.getResult(sootClass.getName(), options, new String[1]);
        for(String key : result.keySet()){
            JvmOutput jvmOutput = result.get(key);
            String stdout = jvmOutput.getStdout();
            if(stdout.contains("TIMEOUT")){
                restore();
                return;
            }
            String p = magicStr + ".*?" + linkStr + "\\S+";
            Pattern pattern = Pattern.compile(p);
            Matcher matcher = pattern.matcher(stdout);
            while(matcher.find()){
                String methodBlockPair = matcher.group();
                blockRunSet.add(methodBlockPair);
            }
        }

        Set<String> methodTmp = new HashSet<>();
        for(String pair : blockRunSet){
            methodBlockSet.remove(pair);
            methodTmp.add(pair.split(linkStr)[0].substring(magicStr.length()));
        }

        for(String methodName : methodTmp){
            List<BlockStruct> blockStructs = methodBlockMap.get(methodName);
            SootMethod sootMethod = signatureMap.get(methodName);
            UnitPatchingChain units = sootMethod.retrieveActiveBody().getUnits();
            currentMethod = sootClass.getMethod(methodName);

            for(int idx = blockStructs.size() - 1; idx > -1; idx --){
                BlockStruct blockStruct = blockStructs.get(idx);
                if(reducedBlockSet.contains(blockStruct.id)){
                    continue;
                }
                if(!methodBlockSet.contains(getMethodBlockPair(sootMethod, blockStruct))){
                    removePrintStmt(blockStruct, units);
                    continue;
                }
                if(blockStruct.type == StructParser.STMT){
                    removeSEQ(blockStruct, units);
                }else {
                    removeNotSEQ(blockStruct, units);
                }
            }

            List<Unit> unitList = new ArrayList<>(units);
            for(Unit unit : unitList){
                if(unit instanceof JInvokeStmt){
                    SootMethod method = ((JInvokeStmt) unit).getInvokeExpr().getMethod();
                    if(methodBlockMap.containsKey(method.getSubSignature()) && !methodTmp.contains(method.getSubSignature())){
                        units.remove(unit);
                    }
                }else if(unit instanceof JAssignStmt){
                    Value rightValue = ((JAssignStmt) unit).rightBox.getValue();
                    if(rightValue instanceof JStaticInvokeExpr){
                        SootMethod method = ((JStaticInvokeExpr) rightValue).getMethod();
                        if(methodBlockMap.containsKey(method.getSubSignature()) && !methodTmp.contains(method.getSubSignature())){
                            Type retType = method.getReturnType();
                            Local local = (Local) ((JAssignStmt) unit).leftBox.getValue();
                            Unit pre = units.getPredOf(unit);
                            units.remove(unit);
                            AssignStmt assignStmt = Jimple.v().newAssignStmt(local, PrimitiveValueProvider.next(retType));
                            if(pre == null){
                                units.addFirst(assignStmt);
                            }else {
                                units.insertAfter(assignStmt, pre);
                            }
                        }
                    }else if(rightValue instanceof JVirtualInvokeExpr){
                        SootMethod method = ((JVirtualInvokeExpr) rightValue).getMethod();
                        if(methodBlockMap.containsKey(method.getSubSignature()) && !methodTmp.contains(method.getSubSignature())){
                            Type retType = method.getReturnType();
                            Local local = (Local) ((JAssignStmt) unit).leftBox.getValue();
                            Unit pre = units.getPredOf(unit);
                            units.remove(unit);
                            AssignStmt assignStmt = Jimple.v().newAssignStmt(local, PrimitiveValueProvider.next(retType));
                            if(pre == null){
                                units.addFirst(assignStmt);
                            }else {
                                units.insertAfter(assignStmt, pre);
                            }
                        }
                    }
                }
            }
        }

        SootMethod main = sootClass.getMethodByName("main");
        for(String methodName : methodBlockMap.keySet()){
            if(!methodTmp.contains(methodName) && !methodName.equals(main.getSubSignature())){
                sootClass.getMethods().remove(sootClass.getMethod(methodName));
                sootClass.getMethod(methodName).retrieveActiveBody().getTraps().clear();
            }
        }
    }

    private boolean generalRemoveJudge(Unit unit){
        return !(unit instanceof ReturnStmt) && !(unit instanceof JReturnVoidStmt) && !structParser.isDepended(unit, new ArrayList<>(), reducedUnits, methodUnitIdxMap.get(currentMethod.getSubSignature()), methodLocalDef.get(currentMethod.getSubSignature()), methodLocalDependency.get(currentMethod.getSubSignature()));
    }

    private void removeNotSEQ(BlockStruct blockStruct, UnitPatchingChain units){
        List<Unit> unitList = blockStruct.unitList;
        removePrintStmt(blockStruct, units);
        Collections.reverse(unitList);
        for(Unit unit : unitList){
            if(generalRemoveJudge(unit)){
                if(structParser.blockTrapMap.containsKey(unit)){
                    currentMethod.getActiveBody().getTraps().removeAll(structParser.blockTrapMap.get(unit));
                }
                units.remove(unit);
                reducedUnits.add(unit);
            }else if(unit instanceof ReturnStmt){
                Type type = currentMethod.getReturnType();
                Value newValue = PrimitiveValueProvider.next(type);
                if(newValue != null){
                    ((ReturnStmt) unit).setOp(newValue);
                }
            }
        }

        addReducedBlock(blockStruct);
    }

    private void removeSEQ(BlockStruct blockStruct, UnitPatchingChain units){
        List<Unit> unitList = blockStruct.unitList;
        removePrintStmt(blockStruct, units);
        Collections.reverse(unitList);
        for(Unit unit : unitList){
            if(!(unit instanceof IdentityStmt) && generalRemoveJudge(unit) && !branchSet.contains(unit)){
                units.remove(unit);
                reducedUnits.add(unit);
            }else if(unit instanceof ReturnStmt){
                Type type = currentMethod.getReturnType();
                Value newValue = PrimitiveValueProvider.next(type);
                if(newValue != null){
                    ((ReturnStmt) unit).setOp(newValue);
                }
            }
        }
        addReducedBlock(blockStruct);
    }

    private void addReducedBlock(BlockStruct blockStruct){
        reducedBlockSet.add(blockStruct.id);
        for(BlockStruct sub : blockStruct.subStruct){
            reducedBlockSet.add(sub.id);
        }
    }

    private void removePrintStmt(BlockStruct blockStruct, UnitPatchingChain units){
        units.removeAll(block2PrintMap.get(blockStruct));
        for(BlockStruct sub : blockStruct.subStruct){
            removePrintStmt(sub, units);
        }
    }

    private void restore(){
        for(SootMethod sootMethod : sootClass.getMethods()){
            if(sootMethod.isConstructor() || sootMethod.getName().equals("<clinit>")){
                continue;
            }
            List<BlockStruct> blockStructs = methodBlockMap.get(sootMethod.getSubSignature());
            UnitPatchingChain units = sootMethod.retrieveActiveBody().getUnits();
            for(BlockStruct blockStruct : blockStructs){
                removePrintStmt(blockStruct, units);
            }
        }
    }
}
