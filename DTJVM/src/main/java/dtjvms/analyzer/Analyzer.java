package dtjvms.analyzer;

import dtjvms.executor.JIT.JvmOutput;

import java.util.HashMap;

public class Analyzer {

    public Object analysis(HashMap<String, JvmOutput> o){
        return o;
    }

    public Object analysis(String classname, HashMap<String, JvmOutput> o, int checkType){return o;}

    public Object analysis(String classname, HashMap<String, JvmOutput> o){
        return o;
    }
}
