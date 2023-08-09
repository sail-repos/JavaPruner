package Reducer;

import Utils.ClassUtil;
import com.alibaba.fastjson.JSONObject;
import dtjvms.DTConfiguration;
import dtjvms.DTPlatform;
import dtjvms.DTProperties;
import dtjvms.JvmInfo;
import dtjvms.executor.DTExecutor;
import dtjvms.executor.JIT.JvmOutput;
import dtjvms.loader.DTLoader;
import soot.*;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ReducerContainer {

    private ReduceChain reduceChain;

    private Checker checker;

    private final ArrayList<JvmInfo> jvmCmds;

    private final String ROOT;
    private final String REDUCE_CLASS_PATH;
    private final String REDUCE_CHECKDIR;

    public ReducerContainer(){

        DTProperties dtProperties = DTProperties.getInstance();
        ROOT = dtProperties.getProperty(DTProperties.TARGET_PROJECTS_KEY);
        REDUCE_CHECKDIR = ROOT + DTPlatform.FILE_SEPARATOR + dtProperties.getProperty(DTProperties.PROJECT_REDUCE_DIR);
        REDUCE_CLASS_PATH = ROOT + DTPlatform.FILE_SEPARATOR + dtProperties.getProperty(DTProperties.PROJECT_REDUCE_DIR);

        DTConfiguration.setJvmDepensRoot("01JVMS");
        DTConfiguration.setProjectDepensRoot(ROOT + DTPlatform.FILE_SEPARATOR + "02Benchmarks");

        jvmCmds = DTLoader.getInstance().loadJvms();
        checker = new Checker(REDUCE_CHECKDIR, jvmCmds);
        InsertionReducer insertionReducer = new InsertionReducer(checker, REDUCE_CLASS_PATH);
        MethodReducer methodReducer = new MethodReducer(checker, REDUCE_CLASS_PATH);
        BlockReducer blockReducer = new BlockReducer(checker, REDUCE_CLASS_PATH);

        // 设置约减链
//        reduceChain = new ReduceChain();
//        reduceChain.addReducer(insertionReducer);
        reduceChain.addReducer(methodReducer);
        reduceChain.addReducer(blockReducer);
    }

    public void reduce(){

        File dir = new File(REDUCE_CLASS_PATH);
        if(!dir.exists() || !dir.isDirectory()){
            System.out.println("ROOT PATH INVALID");
            return;
        }

        String resource = System.getProperty("user.dir") + DTPlatform.FILE_SEPARATOR + "reduceProperties" + DTPlatform.FILE_SEPARATOR;
        List<Map<String, String>> reduceClasses = getJsonString(resource + "reduceClass.json");

        for(Map<String, String> classFile : reduceClasses) {
            G.reset();
            ClassUtil.initSootEnvWithClassPath(REDUCE_CLASS_PATH);

            SootClass sootClass = pretreatment(classFile);

            ArrayList<String> options = new ArrayList<>(Arrays.asList(classFile.get("options").split(" ")));
            HashMap<String, JvmOutput> result = DTExecutor.getInstance().dtSingleClass(jvmCmds, options, REDUCE_CLASS_PATH, "project", sootClass.getName(), false, new String[1]);

//            reduceChain.startReduce(result, options);
            System.out.println(sootClass.getName() + "...................DONE");
        }

        System.out.println("ALL DONE!!!");
    }

    private SootClass pretreatment(Map<String, String> classFile){

        // 设置check类路径
        String mainClass = classFile.get("className");
        String[] classPath = mainClass.split("\\.");
        String checkPath = REDUCE_CHECKDIR;
        if(classPath.length > 1){
            for(int i = 0; i < classPath.length - 1; i++){
                String p = classPath[i];
                checkPath = checkPath + DTPlatform.FILE_SEPARATOR + p;
                File file = new File(checkPath);
                if(!file.exists()){
                    file.mkdir();
                }
            }
        }
        checker.setDir(checkPath);

        SootClass sootClass = ClassUtil.loadClass(mainClass);

        // 删除sootclass中所有的输出语句
//        Set<Unit> tmp = new HashSet<>();
//        for(SootMethod sootMethod : sootClass.getMethods()){
//            if(sootMethod.getName().equals("<clinit>")){
//                continue;
//            }
//
//            tmp.clear();
//
//            UnitPatchingChain units = sootMethod.retrieveActiveBody().getUnits();
//            for(Unit unit : units){
//                if(unit instanceof JInvokeStmt){
//                    Value value = ((JInvokeStmt) unit).getInvokeExpr();
//                    if(value instanceof JVirtualInvokeExpr){
//                        SootMethodRef sootMethodRef = ((JVirtualInvokeExpr) value).getMethodRef();
//                        if(sootMethodRef.getName().equals("println")){
//                            tmp.add(unit);
//                        }
//                    }
//                }
//            }
//
//            units.removeAll(tmp);
//        }

        return sootClass;
    }


    /**
     * 读取json文件
     *
     * @param filepath json文件路径
     * @return 返回json字符串
     */
    private List<Map<String, String>> getJsonString(String filepath) {

        StringBuffer sb = new StringBuffer();
        try {
            Reader reader = new InputStreamReader(Files.newInputStream(Paths.get(filepath)), StandardCharsets.UTF_8);
            int ch;
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JSONObject.parseObject(sb.toString().replaceAll("\r\n", ""), List.class);
    }

}
