package JimpleMixer.blocks;

import soot.*;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.ZonedBlockGraph;
import soot.util.Chain;

import java.util.ArrayList;
import java.util.List;

public class MyBlockContainer {

    public static List<BlockInfo> getBlockFromMethod(SootClass sootClass, SootMethod sootMethod){
        List<BlockInfo> allBlocks = new ArrayList<>();
        Body methodBody = sootMethod.retrieveActiveBody();
        // 01 stmt-block, if-block , switch-block, return-block, TrapBlock
        BlockGraph blockGraph = new ZonedBlockGraph(methodBody);
        for (Block block : blockGraph.getBlocks()) {
            BlockInfo currentBlock = BlockAnalyzer.initialBlock(sootClass, block, methodBody);
            if (currentBlock != null){
                allBlocks.add(currentBlock);
            }
        }
        //02 for-block
        LoopNestTree loopNestTree = new LoopNestTree(methodBody);
        for (Loop loop : loopNestTree) {

            BlockInfo loopBlock = BlockAnalyzer.initialLoopBlock(methodBody, allBlocks, loop);
            if (loopBlock != null){
                allBlocks.add(loopBlock);
            }
        }
        return allBlocks;
    }
}
