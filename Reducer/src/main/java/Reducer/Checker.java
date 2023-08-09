package Reducer;

import dtjvms.JvmInfo;
import dtjvms.analyzer.DiffCore;
import dtjvms.analyzer.JDKAnalyzer;
import dtjvms.executor.DTExecutor;
import dtjvms.executor.JIT.JvmOutput;

import java.util.*;

public class Checker {

    private String dir;
    private String projectPath;
    private ArrayList<JvmInfo> jvms;

    public static final int EXCEPTION = 1;
    public static final int CHECKSUM = 2;

    public static final Map<String, Integer> TEST_TYPE_MAP;

    static {
        TEST_TYPE_MAP = new HashMap<>();
        TEST_TYPE_MAP.put("exception", EXCEPTION);
        TEST_TYPE_MAP.put("checksum", CHECKSUM);
    }

    public Checker(ArrayList<JvmInfo> jvms){
        this.jvms = jvms;
    }

    public Checker(String classDir, ArrayList<JvmInfo> jvms){
        this.dir = classDir;
        this.jvms = jvms;
    }

    public String getDir(){
        return dir;
    }

    public void setDir(String dir){
        this.dir = dir;
    }

    public void setProjectPath(String projectPath){
        this.projectPath  = projectPath;
    }

    /**
     * 检查bug是否依旧存在
     * @param originOutput 原始output信息
     * @param sootClass 待检查class
     * @param pOtions jvm options
     * @return 是否存在
     */
    public boolean bugChecker(HashMap<String, JvmOutput> originOutput, String sootClass, ArrayList<String> pOtions, String[] args, int checkType){
        Map<String, JvmOutput> currentResults = DTExecutor.getInstance().dtSingleClass(jvms, pOtions, projectPath, "project", sootClass, false, args);
        switch (checkType){
            case EXCEPTION:
                for (Map.Entry<String, JvmOutput> entry : currentResults.entrySet()) {
                    JvmOutput origin = originOutput.get(entry.getKey());
                    if(!(JDKAnalyzer.getInstance().checkExceptionDiff(new HashMap<String, JvmOutput>() {
                        {
                            put(entry.getKey() + "-origin", origin);
                            put(entry.getKey(), entry.getValue());
                        }
                    }))) {
                        return false;
                    }
                }
                break;
            case CHECKSUM:
                return JDKAnalyzer.getInstance().checksum(new HashMap<>(currentResults), originOutput);
            default:

        }
        return true;
    }

    public Map<String, JvmOutput> getResult(String sootClass, ArrayList<String> pOtions, String[] args){
        return DTExecutor.getInstance().dtSingleClass(jvms, pOtions, projectPath, "project", sootClass, false, args);
    }
}
