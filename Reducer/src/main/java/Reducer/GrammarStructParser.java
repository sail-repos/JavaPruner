package Reducer;

import JimpleMixer.blocks.*;
import soot.*;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.JIfStmt;
import soot.toolkits.graph.Block;

import java.util.*;

public class GrammarStructParser extends StructParser{

    public GrammarStructParser(List<BlockInfo> blockInfos, Body body){
        super(blockInfos, body);
        branchSet = new HashSet<>();
    }

    @Override
    public void parse() {
        structMap.clear();
        structList.clear();
        localDefMap.clear();
        localDependencyMap.clear();
        localTreatment();

        Map<Integer, Integer> loopHeadMap = new HashMap<>();
        Set<Integer> notIfHead = new HashSet<>();

        int unitIdx = 0;
        for(int idx = 0; idx < blockInfos.size(); idx++){
            BlockInfo blockInfo = blockInfos.get(idx);
            for (Block block : blockInfo.getCurrentBlock().getPreds()) {
                blockInfo.getPres().add(block.getIndexInMethod());
            }
            for (Block block : blockInfo.getCurrentBlock().getSuccs()) {
                blockInfo.getSuccs().add(block.getIndexInMethod());
            }
            for(Unit unit : blockInfo.getAllStmts()){
                unitIdxMap.put(unit, unitIdx++);
                unitBlockMap.put(unit, blockInfo.getCurrentBlock().getIndexInMethod());
            }
            if(blockInfo instanceof LoopStmtBlockInfo){
                List<BlockInfo> loopBlocks = ((LoopStmtBlockInfo) blockInfo).getLoopBlocks();
                Unit loopHead = loopBlocks.get(0).getAllStmts().get(0);
                List<Unit> tailUnits = loopBlocks.get(loopBlocks.size() - 1).getAllStmts();
                Unit loopExit = tailUnits.get(tailUnits.size() - 1);
                loopHeadMap.put(unitBlockMap.get(loopHead), idx);
                if(loopHead instanceof JIfStmt){
                    if(loopExit instanceof JIfStmt && ((JIfStmt) loopExit).getTarget() == loopHead){   // 防止do-while结构中开头就是if
                        continue;
                    }
                    notIfHead.add(unitBlockMap.get(loopHead));
                }
            }
        }

        Map<Integer, List<Trap>> trapInfos = trapPretreatment();

        for(BlockInfo blockInfo : blockInfos){
            if(blockInfo instanceof LoopStmtBlockInfo){
                continue;
            }
            if(trapInfos.containsKey(blockInfo.getCurrentBlock().getIndexInMethod())){
                for(Trap trap : trapInfos.get(blockInfo.getCurrentBlock().getIndexInMethod())){
                    trapAnalyse(trap);
                }
            }
            if(loopHeadMap.containsKey(blockInfo.getCurrentBlock().getIndexInMethod())){
                loopAnalyse((LoopStmtBlockInfo) blockInfos.get(loopHeadMap.get(blockInfo.getCurrentBlock().getIndexInMethod())));
            }
            if(blockInfo instanceof IfStmtBlockInfo && !notIfHead.contains(blockInfo.getCurrentBlock().getIndexInMethod())){
                ifAnalyse((IfStmtBlockInfo) blockInfo);
            }else if(blockInfo instanceof SwitchStmtBlockInfo){
                switchAnalyse((SwitchStmtBlockInfo) blockInfo);
            }

            if(blockInfo instanceof StmtBlockInfo || blockInfo instanceof TrapStmtBlockInfo){
                seqAnalyse(blockInfo);
            }

        }

//        handleStructs();
    }

    private Map<Integer, List<Trap>> trapPretreatment(){
        Map<Integer, List<Trap>> result = new HashMap<>();
        for(Trap trap : body.getTraps()){
            int blockIdx = unitBlockMap.get(trap.getBeginUnit());
            List<Trap> list = result.getOrDefault(blockIdx, new ArrayList<>());
            list.add(trap);
            result.put(blockIdx, list);
        }
        return result;
    }

    private void seqAnalyse(BlockInfo blockInfo){
        List<Integer> structNodeList = new ArrayList<>();
        int nodeIdx = blockInfo.getCurrentBlock().getIndexInMethod();
        structNodeList.add(nodeIdx);
        List<Unit> unitList = blockInfo.getAllStmts();
        unitList.removeIf(unit -> unit instanceof IdentityStmt);
        if(unitList.size() > 0) {
            BlockStruct struct = new BlockStruct(structIdx, StructParser.STMT, nodeIdx, nodeIdx, structNodeList, unitList);
            structMap.put(structIdx++, struct);
            structList.add(struct);
        }
    }

    private void loopAnalyse(LoopStmtBlockInfo loopStmtBlockInfo){
        List<BlockInfo> loopContent = loopStmtBlockInfo.getLoopBlocks();
        UnitPatchingChain units = body.getUnits();
        Comparator<BlockInfo> comparator = new Comparator<BlockInfo>() {
            @Override
            public int compare(BlockInfo o1, BlockInfo o2) {
                return o1.getCurrentBlock().getIndexInMethod() - o2.getCurrentBlock().getIndexInMethod();
            }
        };
        loopContent.sort(comparator);

        int headNode = loopContent.get(0).getCurrentBlock().getIndexInMethod();
        int exitNode = loopContent.get(loopContent.size() - 1).getCurrentBlock().getIndexInMethod();
        BlockStruct blockStruct = new BlockStruct(structIdx, LOOP, headNode, exitNode);
        for(BlockInfo contentBlock : loopContent){
            blockStruct.unitList.addAll(contentBlock.getAllStmts());
        }

        int loopSize = blockStruct.unitList.size();
        int idxBias = unitIdxMap.get(blockStruct.unitList.get(loopSize - 1)) - unitIdxMap.get(blockStruct.unitList.get(0));
        if(loopSize - 1 < idxBias){
            Unit unit = blockStruct.unitList.get(0);
            Unit end = blockStruct.unitList.get(loopSize - 1);
            int idx = 0;
            while(unit != end){
                if(!blockStruct.unitList.contains(unit)){
                    blockStruct.unitList.add(idx, unit);
                }
                unit = units.getSuccOf(unit);
                idx++;
            }
        }

        for(Unit unit : blockStruct.unitList){
            int nodeIdx = unitBlockMap.get(unit);
            if(!blockStruct.nodeList.contains(nodeIdx)){
                blockStruct.nodeList.add(nodeIdx);
            }
        }

        Unit exitUnit = blockStruct.unitList.get(blockStruct.unitList.size() - 1);
        Unit headUnit = blockStruct.unitList.get(0);

        Unit loopConditionUnit = null;
        if(headUnit instanceof JIfStmt && ((JIfStmt) headUnit).getTarget() == units.getSuccOf(exitUnit)){
            branchSet.add(headUnit);
            blockStruct.branchSet.add(headUnit);
            loopConditionUnit = headUnit;
        }else {
            loopConditionUnit = exitUnit;
        }
        branchSet.add(exitUnit);
        blockStruct.branchSet.add(exitUnit);

        Set<Unit> loopConditionUnits = new HashSet<>();
        for(ValueBox valueBox : loopConditionUnit.getUseBoxes()){
            Value value = valueBox.getValue();
            if(value instanceof Local){
                loopConditionUnits.addAll(localDefMap.getOrDefault((Local) value, new ArrayList<>()));
            }
        }

        blockStruct.loopIndexes = new HashSet<>();
        for(Unit unit : blockStruct.unitList){
            if(loopConditionUnits.contains(unit)){
                blockStruct.loopIndexes.add(unit);
            }
        }

        this.globalLoopIndexes.addAll(blockStruct.loopIndexes);

        structList.add(blockStruct);
        structMap.put(structIdx++, blockStruct);
    }

    private void ifAnalyse(IfStmtBlockInfo ifStmtBlockInfo){
        UnitPatchingChain units = body.getUnits();
        List<Unit> ifStmts = ifStmtBlockInfo.getAllStmts();
        IfStmt ifStart = null;
        List<Unit> stmts = new ArrayList<>();
        for(Unit unit : ifStmts){
            if(unit instanceof IfStmt){
                ifStart = (IfStmt) unit;
                break;
            }
            stmts.add(unit);
        }
        if(stmts.size() > 0){
            stmts.removeIf(unit -> unit instanceof IdentityStmt);
            if(stmts.size() > 0) {
                BlockStruct struct = new BlockStruct(structIdx, StructParser.STMT, 0, 0, new ArrayList<>(), stmts);
                structMap.put(structIdx++, struct);
                structList.add(struct);
            }
        }
        if(branchSet.contains(ifStart)){
            return;
        }
        Unit cur = ifStart;
        Unit branchExit = ifStart.getTarget();
        List<Integer> ifBlockContent = new ArrayList<>();
        List<Unit> ifUnitContent = new ArrayList<>();
        Unit ifExit = null;

        while(cur != branchExit && cur != null){
            ifUnitContent.add(cur);
            if(cur instanceof GotoStmt){
                if(unitIdxMap.get(((GotoStmt) cur).getTarget()) > unitIdxMap.get(branchExit)){
                    ifExit = ((GotoStmt) cur).getTarget();
                }
            }

            cur = units.getSuccOf(cur);

            if(branchSet.contains(cur)){
                break;
            }
        }

        Set<Unit> tmp = new HashSet<>();
        tmp.add(ifStart);

        if(ifExit != null){
            tmp.add(branchExit);
            tmp.add(units.getPredOf(branchExit));
            while(cur != ifExit){
                ifUnitContent.add(cur);
                cur = units. getSuccOf(cur);
                if(branchSet.contains(cur)){
                    break;
                }
            }
        }


        for(Unit unit : ifUnitContent){
            int nodeIdx = unitBlockMap.get(unit);
            if(!ifBlockContent.contains(nodeIdx)){
                ifBlockContent.add(nodeIdx);
            }
        }

        BlockStruct blockStruct = new BlockStruct(structIdx, IF, ifStmtBlockInfo.getCurrentBlock().getIndexInMethod(), ifBlockContent.get(ifBlockContent.size() - 1), ifBlockContent, ifUnitContent);
        blockStruct.branchSet.addAll(tmp);
        branchSet.addAll(tmp);
        structList.add(blockStruct);
        structMap.put(structIdx++, blockStruct);
    }

    private void switchAnalyse(SwitchStmtBlockInfo switchStmtBlockInfo){
        UnitPatchingChain units = body.getUnits();
        List<Integer> switchGotoBranch = switchStmtBlockInfo.getSuccs();
        Unit startUnit = blockInfos.get(switchGotoBranch.get(0)).getAllStmts().get(0);
        Unit lastBranchUnit = blockInfos.get(switchGotoBranch.get(switchGotoBranch.size() - 1)).getAllStmts().get(0);
        List<Integer> switchBlockContent = new ArrayList<>();
        List<Unit> switchUnitContent = new ArrayList<>();
        Unit switchStmt = switchStmtBlockInfo.getAllStmts().get(switchStmtBlockInfo.getAllStmts().size() - 1);
//        switchUnitContent.add(switchStmt);

        Unit cur = switchStmt;
        while(cur != lastBranchUnit){
            switchUnitContent.add(cur);
            cur = units.getSuccOf(cur);
            if(branchSet.contains(cur)){
                break;
            }
        }

        Unit exitUnit = null;

        for(Unit unit : switchUnitContent){
            if(unit instanceof GotoStmt){
                Unit tar = ((GotoStmt) unit).getTarget();
                if(!switchUnitContent.contains(tar)){
                    exitUnit = tar;
                    break;
                }
            }else if(unit instanceof JIfStmt){
                Unit tar = ((JIfStmt) unit).getTarget();
                if(!switchUnitContent.contains(tar)){
                    exitUnit = tar;
                    break;
                }
            }
        }

        while(cur != exitUnit){
            switchUnitContent.add(cur);
            cur = units.getSuccOf(cur);
            if(branchSet.contains(cur)){
                break;
            }
        }

        if(exitUnit == null){
            exitUnit = switchUnitContent.get(switchUnitContent.size() - 1);
        }

        Set<Unit> tmp = new HashSet<>();
        for(int caseIdx : switchGotoBranch){
            List<Unit> caseList = blockInfos.get(caseIdx).getAllStmts();
            Unit branch = caseList.get(0);
            if(unitIdxMap.get(branch) < unitIdxMap.get(exitUnit)){
                tmp.add(branch);
            }
        }

        for(Unit unit : switchUnitContent){
            int nodeIdx = unitBlockMap.get(unit);
            if(!switchBlockContent.contains(nodeIdx)){
                switchBlockContent.add(nodeIdx);
            }
        }

        branchSet.addAll(tmp);
        BlockStruct blockStruct = new BlockStruct(structIdx, SWITCH, switchStmtBlockInfo.getCurrentBlock().getIndexInMethod(), unitBlockMap.get(exitUnit), switchBlockContent, switchUnitContent);
        blockStruct.branchSet.addAll(tmp);
        structList.add(blockStruct);
        structMap.put(structIdx++, blockStruct);
    }

    private void trapAnalyse(Trap trap){
        BlockStruct blockStruct = new BlockStruct();
        UnitPatchingChain units = body.getUnits();
        Unit beginUnit = trap.getBeginUnit();
        Unit endUnit = trap.getEndUnit();
        List<Integer> trapBlockContent = new ArrayList<>();
        List<Unit> trapUnitContent = new ArrayList<>();
        Unit cur = beginUnit;
        Unit trapExit = null;
        Unit stopUnit = endUnit instanceof GotoStmt ? units.getSuccOf(endUnit) : endUnit;
//        Unit stopUnit = units.getSuccOf(endUnit);
        while(cur != stopUnit){
            trapUnitContent.add(cur);
            cur = units.getSuccOf(cur);
        }

        if(endUnit instanceof GotoStmt){
            Unit tar = ((GotoStmt) endUnit).getTarget();
            if(unitIdxMap.get(tar) > unitIdxMap.get(endUnit)){
                branchSet.add(cur);
                branchSet.add(endUnit);
                blockStruct.branchSet.add(cur);
                blockStruct.branchSet.add(endUnit);
                trapExit = tar;
            }
        }else {
            Unit throwUnit = units.getPredOf(endUnit);
            if(throwUnit instanceof ThrowStmt) {
                branchSet.add(throwUnit);
                Local throwLocal = (Local) throwUnit.getUseBoxes().get(0).getValue();
                List<Unit> throwDependencies = localDependencyMap.getOrDefault(throwLocal, new ArrayList<>());
                throwDependencies.add(endUnit);
                localDependencyMap.put(throwLocal, throwDependencies);
            }
        }

        if(trapExit != null){
            while(cur != trapExit){
                trapUnitContent.add(cur);
                cur = units.getSuccOf(cur);
                if(branchSet.contains(cur)){
                    break;
                }

                if(cur instanceof GotoStmt){
                    Unit tar = ((GotoStmt) cur).getTarget();
                    if(tar == trapExit){
                        branchSet.add(cur);
                        blockStruct.branchSet.add(cur);
                    }
                }
            }
        }

        for(Unit unit : trapUnitContent){
            int nodeIdx = unitBlockMap.get(unit);
            if(!trapBlockContent.contains(nodeIdx)){
                trapBlockContent.add(nodeIdx);
            }
        }

        if(!(endUnit instanceof GotoStmt)){
            trapUnitContent.add(endUnit);
        }

        blockStruct.id = structIdx;
        blockStruct.type = TRAP;
        blockStruct.startNode = unitBlockMap.get(beginUnit);
        blockStruct.destNode = unitBlockMap.get(cur);
        blockStruct.nodeList = trapBlockContent;
        blockStruct.unitList = trapUnitContent;
        blockStruct.branchSet.add(beginUnit);

        Set<Trap> trapSet = blockTrapMap.getOrDefault(trap.getHandlerUnit(), new HashSet<>());
        trapSet.add(trap);
        blockTrapMap.put(trap.getHandlerUnit(), trapSet);
        structList.add(blockStruct);
        structMap.put(structIdx++, blockStruct);
    }

//    @Override
//    protected void handleStructs(){
//        Comparator<BlockStruct> comparator = new Comparator<BlockStruct>() {
//            @Override
//            public int compare(BlockStruct o1, BlockStruct o2) {
//                if(o1.unitList.size() != o2.unitList.size()) {
//                    return o1.unitList.size() - o2.unitList.size();
//                }else{
//                    return o1.type - o2.type;
//                }
//            }
//        };
//
//        List<BlockStruct> sortList = new ArrayList<>(structList);
//        sortList.sort(comparator);
//
//        Map<Unit, Integer> node2Block = new HashMap<>();
//        Set<BlockStruct> children = new HashSet<>();
//
//        for(BlockStruct blockStruct : sortList){
//            boolean isContain = false;
//            List<BlockStruct> subStruct = new ArrayList<>();
//            for(Unit unit : blockStruct.unitList){
//                if(node2Block.containsKey(unit)){
//                    isContain = true;
//                    subStruct.add(structMap.get(node2Block.get(unit)));
//                    node2Block.remove(unit);
//                }
//            }
//
//            node2Block.put(blockStruct.unitList.get(0), blockStruct.id);
//
//            if(isContain){
//                blockStruct.subStruct.addAll(subStruct);
//                children.addAll(subStruct);
//            }else {
//                blockStruct.isBasic = true;
//            }
//        }
//
//        sortList.clear();
//        for(BlockStruct blockStruct : structList){
//            if(!children.contains(blockStruct)){
//                sortList.add(blockStruct);
//            }
//        }
//
//        structList.clear();
//        structList = sortList;
//
//    }

}
