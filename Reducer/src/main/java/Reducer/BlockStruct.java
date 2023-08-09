package Reducer;

import soot.Unit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockStruct implements Comparable{

    public int id;

    public Unit head;
    public int startNode;
    public int destNode;
    public List<Integer> nodeList;
    public List<Unit> unitList;
    public int type;
    public Set<Unit> branchSet;
    public Set<Unit> loopIndexes;  // loop结构专用
    public boolean isBasic = false;
    public boolean isDelete = false;

    public List<BlockStruct> subStruct;

    public BlockStruct(){
        unitList = new ArrayList<>();
        nodeList = new ArrayList<>();
        branchSet = new HashSet<>();
        subStruct = new ArrayList<>();
    }

    public BlockStruct(int id, int type, int startNode, int destNode){
        this.id = id;
        this.type = type;
        this.startNode = startNode;
        this.destNode = destNode;
        unitList = new ArrayList<>();
        nodeList = new ArrayList<>();
        branchSet = new HashSet<>();
        subStruct = new ArrayList<>();
    }

    public BlockStruct(List<Integer> nodeList, List<Unit> unitList){
        this.nodeList = nodeList;
        this.unitList = unitList;
        branchSet = new HashSet<>();
        subStruct = new ArrayList<>();
    }

    public BlockStruct(int id, int type, int startNode, int destNode, List<Integer> nodeList, List<Unit> unitList){
        this.id = id;
        this.type = type;
        this.nodeList = nodeList;
        this.startNode = startNode;
        this.destNode = destNode;
        this.unitList = unitList;
        branchSet = new HashSet<>();
        subStruct = new ArrayList<>();
    }

    public BlockStruct(int id, int type, Unit head, List<Unit> unitList){
        this.id = id;
        this.type = type;
        this.head = head;
        this.unitList = unitList;
        branchSet = new HashSet<>();
        subStruct = new ArrayList<>();
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
