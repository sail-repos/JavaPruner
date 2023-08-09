package Reducer;

import soot.SootMethod;
import soot.Unit;

public class MethodInvokeInfo {
    public Unit invokeUnit;
    public SootMethod parentMethod;

    public MethodInvokeInfo(Unit invokeUnit, SootMethod parentMethod){
        this.invokeUnit = invokeUnit;
        this.parentMethod = parentMethod;
    }
}
