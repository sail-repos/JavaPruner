package Reducer;

import JimpleMixer.blocks.BlockInfo;
import soot.*;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.annotation.logic.LoopFinder;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

import java.util.*;

public class CFGStructParser extends StructParser{

    private UnitGraph unitGraph;

    private final MHGDominatorsFinder<Unit> mhgDominatorsFinder;

    private final MHGPostDominatorsFinder<Unit> mhgPostDominatorsFinder;
    private final Map<Unit, Integer> unitOrder;

    public CFGStructParser(List<BlockInfo> blockInfos, Body body){
        super(blockInfos, body);
        unitGraph = new MyUnitGraph(body);
        mhgDominatorsFinder = new MHGDominatorsFinder<>(unitGraph);
        mhgPostDominatorsFinder = new MHGPostDominatorsFinder<>(unitGraph);
        unitOrder = new HashMap<>();

        int order = 0;
        for(Unit u : unitGraph){
            unitOrder.put(u, order++);
        }
    }

    @Override
    public void parse() {
        structMap.clear();
        structList.clear();
        localDefMap.clear();
        localDependencyMap.clear();
        localTreatment();

        int idx = 0;
        for(Unit u : body.getUnits()){
            unitIdxMap.put(u, idx++);
        }

        Map<Unit, Loop> loopInfo = handleLoop();
        Map<Unit, List<Trap>> trapInfo = trapPretreatment();
        for(Unit u : unitGraph){
            if(trapInfo.containsKey(u)){
                for(Trap trap : trapInfo.get(u)){
                    handleTrap(trap);
                }
            }

            if(u instanceof IfStmt && !loopInfo.containsKey(u)){
                handleIF(u);
            }else if(u instanceof SwitchStmt){
                handleSwitch(u);
            }else if(loopInfo.containsKey(u)){
                postHandleLoop(loopInfo.get(u));
            }

            // 判断是否为顺序起始节点
            if(judgeSEQHead(u)){
                handleSEQ(u);
            }

        }

    }

    public boolean judgeSEQHead(Unit u){

        List<Unit> pres = unitGraph.getPredsOf(u);
        List<Unit> succs = unitGraph.getSuccsOf(u);

        if(pres.size() == 0){
            return true;
        }

        if(pres.size() > 1 && succs.size() <= 1){
            return true;
        }

        if(pres.size() == 1 && succs.size() == 1){
            Unit pre = pres.get(0);
            return unitGraph.getSuccsOf(pre).size() > 1;
        }

        return false;
    }

    private Map<Unit, Loop> handleLoop(){
        Map<Unit, Loop> loopInfo = new HashMap<>();
        LoopFinder loopFinder = new LoopFinder();
        Set<Loop> loops = loopFinder.getLoops(unitGraph);
        for(Loop loop : loops){
            Unit head = loop.getHead();
            loopInfo.put(head, loop);
        }

        return loopInfo;
    }

    public void postHandleLoop(Loop loop){
        BlockStruct blockStruct = new BlockStruct();
        Set<Unit> exits = new HashSet<>(loop.getLoopExits());
        Set<Unit> loopConditionUnits = new HashSet<>();
        for(Unit u : exits){
            for(ValueBox valueBox : u.getUseBoxes()){
                Value value = valueBox.getValue();
                if(value instanceof Local){
                    loopConditionUnits.addAll(localDefMap.getOrDefault(value, new ArrayList<>()));
                }
            }
        }

        blockStruct.id = structIdx;
        blockStruct.type = StructParser.LOOP;
        List<Unit> unitList = new ArrayList<>(loop.getLoopStatements());
        unitSort(unitList);
        blockStruct.unitList = unitList;
        blockStruct.head = loop.getHead();

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

    public void handleSEQ(Unit head){
        List<Unit> SEQStmtContent = new ArrayList<>();
        Unit cur = head;
        while(cur != null){
            SEQStmtContent.add(cur);
            List<Unit> succs = unitGraph.getSuccsOf(cur);
            if(succs.size() == 0){
                break;
            }
            cur = succs.get(0);
            if(unitGraph.getSuccsOf(cur).size() > 1 || unitGraph.getPredsOf(cur).size() > 1){
                break;
            }
        }
        unitSort(SEQStmtContent);
        BlockStruct blockStruct = new BlockStruct(structIdx, StructParser.STMT, head, SEQStmtContent);
        structMap.put(structIdx++, blockStruct);
        structList.add(blockStruct);
    }

    public void handleSwitch(Unit head){
        Unit switchExit = getPostDominator(head, 1);
        List<Unit> switchStmtContent = ReverseDFS(head, switchExit);
        if(switchStmtContent.size() == 0){
            return;
        }
        unitSort(switchStmtContent);
        BlockStruct blockStruct = new BlockStruct(structIdx, StructParser.SWITCH, head, switchStmtContent);
        structMap.put(structIdx, blockStruct);
        structList.add(blockStruct);
    }

    public void handleTrap(Trap trap){
        Unit handleUnit = trap.getHandlerUnit();
        Unit trapExit = handleUnit;
        Set<Unit> visited = new HashSet<>();
        Stack<Unit> stack = new Stack<>();
        stack.add(trapExit);
        while(!stack.isEmpty()){
            Unit cur = stack.pop();
            if(visited.contains(cur)){
                continue;
            }

            for(Unit u : unitGraph.getSuccsOf(cur)){
                stack.push(u);
            }
            visited.add(cur);

            if(!(mhgDominatorsFinder.getDominators(cur).contains(handleUnit))){
                trapExit = cur;
                break;
            }
        }
        List<Unit> trapStmtContent = ReverseDFS(trap.getBeginUnit(), trapExit);
        if(trapStmtContent.size() == 0){
            return;
        }
        unitSort(trapStmtContent);

        BlockStruct blockStruct = new BlockStruct(structIdx, StructParser.TRAP, trap.getBeginUnit(), trapStmtContent);
        Unit endUnit = trap.getEndUnit();
        if(endUnit instanceof GotoStmt){
            branchSet.add(endUnit);
            blockStruct.branchSet.add(endUnit);
        }

        Set<Trap> trapSet = blockTrapMap.getOrDefault(handleUnit, new HashSet<>());
        trapSet.add(trap);
        blockTrapMap.put(trap.getHandlerUnit(), trapSet);

        structMap.put(structIdx++, blockStruct);
        structList.add(blockStruct);
    }

    public void handleIF(Unit head){
        Unit ifExit = getPostDominator(head, 1);
        List<Unit> ifStmtContent = ReverseDFS(head, ifExit);
        if(ifStmtContent.size() == 0){
            return;
        }
        unitSort(ifStmtContent);
        BlockStruct blockStruct = new BlockStruct(structIdx, StructParser.IF, head, ifStmtContent);
        structMap.put(structIdx++, blockStruct);
        structList.add(blockStruct);
    }

    private Map<Unit, List<Trap>> trapPretreatment(){
        Map<Unit, List<Trap>> trapInfo = new HashMap<>();
        for(Trap trap : body.getTraps()){
            List<Trap> list = trapInfo.getOrDefault(trap.getBeginUnit(), new ArrayList<>());
            list.add(trap);
            trapInfo.put(trap.getBeginUnit(), list);
        }
        return trapInfo;
    }

    private Unit getPostDominator(Unit u, int order){
        List<Unit> postDominators = mhgPostDominatorsFinder.getDominators(u);
        int selfIdx = postDominators.indexOf(u);
        order = order < 0 ? postDominators.size() : order;
        int idx = (selfIdx + order) % postDominators.size();
        return postDominators.get(idx);
    }

    private Unit getExit(Unit dominator){
        Set<Unit> visited = new HashSet<>();
        Stack<Unit> stack = new Stack<>();
        stack.push(dominator);
        while(!stack.isEmpty()){
            Unit cur = stack.pop();
            if(visited.contains(cur)){
                continue;
            }

            for(Unit u : unitGraph.getSuccsOf(cur)){
                stack.push(u);
            }
            visited.add(cur);
            if(!(mhgDominatorsFinder.getDominators(cur).contains(dominator))){
                break;
            }
        }
        return null;
    }

    /**
     * DFS遍历
     * @param start 起始节点
     * @param exit  结束节点
     * @return  遍历结果
     */
    private List<Unit> ReverseDFS(Unit start, Unit exit){
        List<Unit> unitList = new ArrayList<>();
        Set<Unit> visited = new HashSet<>();
        Stack<Unit> stack = new Stack<>();
        List<Unit> startDominators = mhgDominatorsFinder.getDominators(start);
        stack.add(exit);
        while(!stack.isEmpty()){
            Unit u = stack.pop();
            if(visited.contains(u) || u == start || (startDominators.contains(u))){
                continue;
            }
            List<Unit> preds = unitGraph.getPredsOf(u);
            for(int i = preds.size() - 1; i > -1; i--){
                stack.add(preds.get(i));
            }
            visited.add(u);
            unitList.add(0, u);
        }
        unitList.add(0, start);
        unitList.remove(exit);
        return unitList;
    }

    private void unitSort(List<Unit> unitList){
        unitList.sort(new Comparator<Unit>() {
            @Override
            public int compare(Unit o1, Unit o2) {
                return unitOrder.get(o1) - unitOrder.get(o2);
            }
        });
    }

}
