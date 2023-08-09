package Reducer;

import JimpleMixer.blocks.BlockInfo;
import soot.*;
import soot.jimple.BinopExpr;
import soot.jimple.Expr;
import soot.jimple.InvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.*;

import javax.rmi.PortableRemoteObject;
import java.util.*;
import java.util.stream.Collectors;

public abstract class StructParser {

    protected List<BlockInfo> blockInfos;

    protected int structIdx;

    protected Map<Integer, BlockStruct> structMap;
    protected List<BlockStruct> structList;

    protected Map<Unit, Integer> unitBlockMap;

    protected Map<Unit, Integer> unitIdxMap;

    protected Set<Unit> branchSet;

    protected Map<Local, List<Unit>> localDependencyMap;

    protected Map<Local, List<Unit>> localDefMap;

    protected Body body;

    protected Set<Unit> globalLoopIndexes;

    protected Map<Unit, Set<Trap>> blockTrapMap;

    public static Integer STMT = 1;
    public static Integer TRAP = 2;
    public static Integer LOOP = 3;
    public static Integer IF = 4;
    public static Integer SWITCH = 5;

    public StructParser(List<BlockInfo> blockInfos, Body body){
        this.blockInfos = blockInfos;
        structMap = new HashMap<>();
        structList = new ArrayList<>();
        unitBlockMap = new HashMap<>();
        unitIdxMap = new HashMap<>();
        structIdx = blockInfos.size();
        branchSet = new HashSet<>();
        localDefMap = new HashMap<>();
        localDependencyMap = new HashMap<>();
        globalLoopIndexes = new HashSet<>();
        blockTrapMap = new HashMap<>();
        this.body = body;
    }

    public abstract void parse();

    protected void localTreatment(){
        UnitPatchingChain units = body.getUnits();
        Unit cur = units.getFirst();
        while(cur != null){
            for(ValueBox valueBox : cur.getDefBoxes()){
                Value defValue = valueBox.getValue();
                if(defValue instanceof Local){
                    List<Unit> unitList = localDefMap.getOrDefault((Local) defValue, new ArrayList<>());
                    unitList.add(cur);
                    localDefMap.put((Local) defValue, unitList);
                }
            }

            for(ValueBox valueBox : cur.getUseBoxes()){
                Value useValue = valueBox.getValue();
                if(useValue instanceof Local){
                    List<Unit> unitList = localDependencyMap.getOrDefault((Local) useValue, new ArrayList<>());
                    unitList.add(cur);
                    localDependencyMap.put((Local) useValue, unitList);
                }
            }

            cur = units.getSuccOf(cur);
        }
    }

    /**
     * 判读结构是否为空
     * @param blockStruct 结构体
     * @return
     */
    public static boolean isStructEmpty(BlockStruct blockStruct){
        if(blockStruct.isBasic){
            return true;
        }
        for(BlockStruct sub : blockStruct.subStruct){
            if(!sub.isDelete){
                return false;
            }
        }

        blockStruct.isDelete = true;
        return true;
    }

    /**
     * 判断该unit是否被依赖
     * @param unit 待删除unit
     * @param reducedTmp 当前已删除
     * @param reducedUnit 过去已删除
     * @return 返回是否被依赖
     */
    public boolean isDepended(Unit unit, List<Unit> reducedTmp, Set<Unit> reducedUnit){
        Map<Local, List<Unit>> localDef = localDefMap;
        Map<Local, List<Unit>> localDependency = localDependencyMap;
        int unitIdx = unitIdxMap.get(unit);
        for(ValueBox valueBox : unit.getDefBoxes()){
            Value value = valueBox.getValue();
            if(value instanceof Local){
                List<Unit> localDependencyList = localDependency.getOrDefault((Local) value, new ArrayList<>());
                for(Unit dependencyUnit : localDependencyList){
                    if(!reducedTmp.contains(dependencyUnit) && !reducedUnit.contains(dependencyUnit)){
                        List<Unit> localDefList = localDef.getOrDefault((Local) value, new ArrayList<>());
                        int validDefNum = 0;
                        for(Unit defUnit : localDefList){
                            int defIdx = unitIdxMap.get(defUnit);
                            if(defUnit != unit && !reducedTmp.contains(defUnit) && !reducedUnit.contains(defUnit) && defIdx < unitIdx){
                                validDefNum++;
                            }
                        }
                        if(validDefNum == 0){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isDepended(Unit unit, List<Unit> reducedTmp, Set<Unit> reducedUnit, Map<Unit, Integer> unitIdxMap, Map<Local, List<Unit>> localDefMap, Map<Local, List<Unit>> localDependencyMap){
        int unitIdx = unitIdxMap.get(unit);
        for(ValueBox valueBox : unit.getDefBoxes()){
            Value value = valueBox.getValue();
            if(value instanceof Local){
                List<Unit> localDependencyList = localDependencyMap.getOrDefault((Local) value, new ArrayList<>());
                for(Unit dependencyUnit : localDependencyList){
                    if(!reducedTmp.contains(dependencyUnit) && !reducedUnit.contains(dependencyUnit)){
                        List<Unit> localDefList = localDefMap.getOrDefault((Local) value, new ArrayList<>());
                        int validDefNum = 0;
                        for(Unit defUnit : localDefList){
                            int defIdx = unitIdxMap.get(defUnit);
                            if(defUnit != unit && !reducedTmp.contains(defUnit) && !reducedUnit.contains(defUnit) && defIdx < unitIdx){
                                validDefNum++;
                            }
                        }
                        if(validDefNum == 0){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void handleBasicStruct(List<BlockStruct> blockStructs){
        for(BlockStruct blockStruct : blockStructs){
            boolean isBasic = true;
            for(BlockStruct sub : blockStruct.subStruct){
                if(sub.type != STMT){
                    isBasic = false;
                    break;
                }
            }

            if(isBasic){
                blockStruct.subStruct.clear();
                blockStruct.isBasic = true;
            }else {
                handleBasicStruct(blockStruct.subStruct);
            }
        }
    }

    public void handleStructs(){
        Comparator<BlockStruct> comparator = new Comparator<BlockStruct>() {
            @Override
            public int compare(BlockStruct o1, BlockStruct o2) {
                if(o1.unitList.size() != o2.unitList.size()) {
                    return o1.unitList.size() - o2.unitList.size();
                }else{
                    return o1.type - o2.type;
                }
            }
        };

        List<BlockStruct> sortList = new ArrayList<>(structList);
        sortList.sort(comparator);

        Map<Unit, Integer> node2Block = new HashMap<>();
        Set<BlockStruct> children = new HashSet<>();

        for(BlockStruct blockStruct : sortList){
            boolean isContain = false;
            List<BlockStruct> subStruct = new ArrayList<>();
            for(Unit unit : blockStruct.unitList){
                if(node2Block.containsKey(unit)){
                    isContain = true;
                    subStruct.add(structMap.get(node2Block.get(unit)));
                    node2Block.remove(unit);
                }
            }

            node2Block.put(blockStruct.unitList.get(0), blockStruct.id);

            if(isContain){
                blockStruct.subStruct.addAll(subStruct);
                children.addAll(subStruct);
            }else {
                blockStruct.isBasic = true;
            }
        }

        sortList.clear();
        for(BlockStruct blockStruct : structList){
            if(!children.contains(blockStruct)){
                sortList.add(blockStruct);
            }
        }

        structList.clear();
        structList = sortList;

        handleBasicStruct(structList);
    }


}
