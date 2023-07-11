import Configuration.PlatformInfo;
import Reducer.*;
import Utils.ClassUtil;
import dtjvms.DTConfiguration;
import dtjvms.DTPlatform;
import dtjvms.JvmInfo;
import dtjvms.ProjectInfo;
import dtjvms.analyzer.DiffCore;
import dtjvms.analyzer.JDKAnalyzer;
import dtjvms.executor.DTExecutor;
import dtjvms.executor.ExecutorHelper;
import dtjvms.executor.JIT.JITExecutor;
import dtjvms.executor.JIT.JvmOutput;
import dtjvms.loader.DTLoader;
import org.junit.Test;
import polyglot.util.SubtypeSet;
import soot.G;
import soot.Local;
import soot.SootClass;
import utils.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Reduce {

    public static String jvmpath = "";
    public static String projectPath = "./Benchmarks";
    public static String targetProjectName = "";
    public static String diffClassPath = "";
    public static String dataPath = "";

    public static List<String> ids = new ArrayList<>();
    public static boolean isJunit = false;

    static {
        if (PlatformInfo.isMac()){
            jvmpath = "/Users/yingquanzhao/Workspace/JVM/02JIT/Projects/JITFuzzing/01JVMS";
            projectPath = "/Users/yingquanzhao/Workspace/JVM/02JIT/Projects/JITFuzzing/sootOutput";
            diffClassPath = "/Users/yingquanzhao/Workspace/JVM/02JIT/Projects/JITFuzzing/03results/1680734219/diffClasses";
        } else if (PlatformInfo.isLinux()){
        } else if (PlatformInfo.isWin()){
            jvmpath = "D:\\TestReducer\\01JVMS";
            projectPath = "D:\\TestReducer\\HotspotTests-Java-gtc\\HotspotTests-Java\\out\\production\\HotspotTests-Java";
            diffClassPath = "D:\\TestReducer\\dataset-gtc2";
            dataPath = "D:\\master\\JVM-Testing\\Reduce\\dataset";
            ids = new ArrayList<>(Arrays.asList("c12"));
//            projectPath = "D:\\TestReducer\\HotspotTests-Java-gtc\\HotspotTests-Java\\out\\production\\HotspotTests-Java";
//            diffClassPath = "D:\\TestReducer\\4-23\\4-23\\04SynthesisHistory\\1682023620523\\diffClass";
        } else {
            throw new RuntimeException("Unknown System!");
        }
    }
    public static void main(String[] args) {

        /**
         * 01 load JVM
         */
        File dataDir = new File(dataPath + DTPlatform.FILE_SEPARATOR + "Test-Classes");
        for(File classDir : dataDir.listFiles()){
            File classInfoJson = new File(classDir.getAbsolutePath() + DTPlatform.FILE_SEPARATOR + "classInfo.json");
            Map<String, Object> info = DataGen.readJsonMap(classInfoJson);
            String cid = info.get("id").toString();
            if(ids.size() > 0 && !ids.contains(cid)){
                continue;
            }

            String classname = info.get("classname").toString();
            String vmoptions = info.get("vmoptions").toString();
            String classpath = info.get("classpath").toString();
            String project = info.get("project").toString();
            String filename = info.get("filename").toString();
            int testtype = info.get("testtype") == null ? Checker.EXCEPTION : Checker.TEST_TYPE_MAP.get(info.get("testtype").toString());
            ClassInfo classInfo = new ClassInfo(cid, project, classname, classpath, vmoptions, testtype, filename);

            DTConfiguration.setJvmDepensRoot(jvmpath);
            DTPlatform.getInstance();
            ArrayList<JvmInfo> jvmCmds = DTLoader.getInstance().loadJvms();
            ReduceChain reduceChain = new ReduceChain(dataPath, classInfo, jvmCmds);
            projectPath = dataPath + DTPlatform.FILE_SEPARATOR + "Projects" + DTPlatform.FILE_SEPARATOR + project;
            InsertionReducer insertionReducer = new InsertionReducer();
            MethodReducer methodReducer = new MethodReducer();
            BlockReducer blockReducer = new BlockReducer();
//            reduceChain.addReducer(insertionReducer);
            reduceChain.addReducer(methodReducer);
//            reduceChain.addReducer(blockReducer);
            try {
                reduceChain.startReduce();
                System.out.println(classname + "============================DONE!!!");
            }catch (Exception e){
                System.out.println(classname + "================ERROR!!!");
                e.printStackTrace();
            }finally {
                G.reset();
            }
            System.out.println("ALL DONE!!!");
        }

    }

    public static void copyClassToTargetProject(String originClassFolder, String className, ProjectInfo targetProject) {

        String mutateClassPath = originClassFolder + DTPlatform.FILE_SEPARATOR +
                className + ".class";

        String targetFilePath;

        if (isJunit){
            targetFilePath = targetProject.getTestClassPath();
        } else {
            targetFilePath = targetProject.getSrcClassPath();
        }

        if (className.contains(".")){

            String seedClassname = className.substring(0, className.lastIndexOf("_"));
            String package_class = seedClassname.replace(".", "/");
            targetFilePath = targetFilePath + DTPlatform.FILE_SEPARATOR + package_class + ".class";
        } else {
            String seedClassname = className.substring(0, className.lastIndexOf("_"));
            targetFilePath = targetFilePath + DTPlatform.FILE_SEPARATOR + seedClassname + ".class";
        }

        System.out.println(mutateClassPath);
        System.out.println(targetFilePath);
        try {

            File sourceFile = new File(mutateClassPath);
            File targetFile = new File(targetFilePath);
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getCheckPath(String className){
        // 设置check类路径
        String[] classPath = className.split("\\.");
        String checkPath = projectPath;
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
        return checkPath;

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
    }
}
