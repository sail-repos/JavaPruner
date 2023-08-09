package JimpleMixer.blocks;

import soot.Local;
import soot.Unit;
import soot.toolkits.graph.Block;

import java.util.ArrayList;
import java.util.List;

public class BlockInfo implements Comparable{

    private String className;
    private Block currentBlock;
    private List<Local> depensLocals = new ArrayList<>();
    private List<Local> depensGlobals = new ArrayList<>();
    private List<Unit> allStmts = new ArrayList<>();
    private List<Unit> validStmts = new ArrayList<>();
    private float QValue = 99; //default
    private float QVTemp = QValue;
    private int chosenTimes;
    private int errorGenerateTimes;
    private int diffFoundTimes;

    private float reward;

    private List<Integer> succs = new ArrayList<>();
    private List<Integer> pres = new ArrayList<>();
    private int succsNum = 0;
    private int presNum = 0;

    public BlockInfo(String className, Block currentBlock) {
        this.className = className;
        this.currentBlock = currentBlock;
        this.chosenTimes = 0;
        this.errorGenerateTimes = 0;
        this.diffFoundTimes = 0;
        this.reward = 0;
    }

    public BlockInfo(Block currentBlock, List<Local> depensLocals, List<Local> depensGlobals, List<Unit> allStmts, List<Unit> validStmts) {
        this.currentBlock = currentBlock;
        this.depensLocals = depensLocals;
        this.depensGlobals = depensGlobals;
        this.allStmts = allStmts;
        this.validStmts = validStmts;
        this.chosenTimes = 0;
        this.errorGenerateTimes = 0;
        this.diffFoundTimes = 0;
        this.reward = 0;
    }

    public BlockInfo(String className,
                     Block currentBlock,
                     List<Local> depensLocals,
                     List<Local> depensGlobals,
                     List<Unit> allStmts,
                     List<Unit> validStmts,
                     int chosenTimes,
                     int errorGenerateTimes,
                     int diffFoundTimes) {
        this.className = className;
        this.currentBlock = currentBlock;
        this.depensLocals = depensLocals;
        this.depensGlobals = depensGlobals;
        this.allStmts = allStmts;
        this.validStmts = validStmts;
        this.chosenTimes = chosenTimes;
        this.errorGenerateTimes = errorGenerateTimes;
        this.diffFoundTimes = diffFoundTimes;
    }

    public void chosenTimesIncrease(){
        this.chosenTimes++;
    }

    public void errorGenerateTimesIncrease(){
        this.errorGenerateTimes++;
    }

    public void diffFoundTimesIncrease(){
        this.diffFoundTimes++;
    }

    public void updateQTable(){

        float w1 = (float)0.5, w2 = (float) 0.5;
        reward = w1 * EGCoverage() + w2 * DFCoverage();
        QValue = (float) (QValue + (0.5 * (reward - QValue)));
    }

    public float DFCoverage(){

        return (float)diffFoundTimes / (float)chosenTimes;
    }

    public float EGCoverage(){

        return (float)errorGenerateTimes / (float)chosenTimes;
    }

    public int getChosenTimes() {
        return chosenTimes;
    }

    public int getErrorGenerateTimes() {
        return errorGenerateTimes;
    }

    public int getDiffFoundTimes() {
        return diffFoundTimes;
    }

    public float getReward() {
        return reward;
    }

    public float getQValue() {
        return QValue;
    }

    public void setQValue(float QValue) {
        this.QValue = QValue;
    }

    public float getQVTemp() {
        return QVTemp;
    }

    public void setQVTemp(float QVTemp) {
        this.QVTemp = QVTemp;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Block getCurrentBlock() {
        return currentBlock;
    }

    public void setCurrentBlock(Block currentBlock) {
        this.currentBlock = currentBlock;
    }

    public List<Local> getDepensLocals() {
        return depensLocals;
    }

    public void setDepensLocals(List<Local> depensLocals) {
        this.depensLocals = depensLocals;
    }

    public List<Local> getDepensGlobals() {
        return depensGlobals;
    }

    public void setDepensGlobals(List<Local> depensGlobals) {
        this.depensGlobals = depensGlobals;
    }

    public List<Unit> getAllStmts() {
        return allStmts;
    }

    public void setAllStmts(List<Unit> allStmts) {
        this.allStmts = allStmts;
    }

    public List<Unit> getValidStmts() {
        return validStmts;
    }

    public void setValidStmts(List<Unit> validStmts) {
        this.validStmts = validStmts;
    }

    public List<Integer> getSuccs(){
        return this.succs;
    }

    public List<Integer> getPres(){
        return this.pres;
    }

    public int getSuccsNum(){
        return this.succsNum;
    }

    public void setSuccsNum(int succsNum){
        this.succsNum = succsNum;
    }

    public int getPresNum(){
        return this.presNum;
    }

    public void setPresNum(int presNum){
        this.presNum = presNum;
    }

    @Override
    public String toString() {
        return "BlockInfo: " +
                "ClassName: " + className + "\n" +
                "Local Vars: " + depensLocals + "\n" +
                "Valid Stmts: " + validStmts;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
