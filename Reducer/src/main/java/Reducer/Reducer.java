package Reducer;

import dtjvms.executor.JIT.JvmOutput;
import soot.SootClass;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Reducer {

    protected Reducer nextReducer;

    protected Checker checker;

    protected String classPath;

    public Reducer(){
    }

    public Reducer(Checker checker, String classPath){
        this.checker = checker;
        this.classPath = classPath;
    }

    public void setNextReducer(Reducer reducer){
        this.nextReducer = reducer;
    }

    abstract protected boolean doReduce(SootClass sootClass, HashMap<String, JvmOutput> result, ArrayList<String> options, int testtype);

}
