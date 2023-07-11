import Configuration.PlatformInfo;
import Utils.ClassUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import dtjvms.DTConfiguration;
import dtjvms.DTPlatform;
import dtjvms.JvmInfo;
import dtjvms.executor.DTExecutor;
import dtjvms.executor.JIT.JvmOutput;
import dtjvms.loader.DTLoader;
import soot.G;
import soot.SootClass;
import soot.SootMethod;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.*;

public class ResultAnalyse {

    public static String jvmpath = "";
    public static String dataOutputPath = "";
    public static String projectPath = "./Benchmarks";
    public static String targetProjectName = "";
    public static String diffClassPath = "";
    public static String jsonPath = "";
    public static String classJson = "ReduceClass-zk.json";
    public static String classResultJson = classJson.substring(0, classJson.lastIndexOf(".json")) + "-result.json";
    public static String classFilterJson = classJson.substring(0, classJson.lastIndexOf(".json")) + "-filter.json";

    static {
        if (PlatformInfo.isMac()){
            jvmpath = "/Users/yingquanzhao/Workspace/JVM/02JIT/Projects/JITFuzzing/01JVMS";
            projectPath = "/Users/yingquanzhao/Workspace/JVM/02JIT/Projects/JITFuzzing/sootOutput";
            diffClassPath = "/Users/yingquanzhao/Workspace/JVM/02JIT/Projects/JITFuzzing/03results/1680734219/diffClasses";
        } else if (PlatformInfo.isLinux()){
        } else if (PlatformInfo.isWin()){
            jvmpath = "D:\\TestReducer\\01JVMS";
            projectPath = "D:\\TestReducer\\HotspotTests-Java-vb";
            diffClassPath = "D:\\TestReducer\\diffClasses";
            dataOutputPath = "D:\\TestReducer\\reduceResult";
            jsonPath = "D:\\TestReducer\\dataset-zk2";
//            projectPath = "D:\\TestReducer\\HotspotTests-Java-gtc\\HotspotTests-Java\\out\\production\\HotspotTests-Java";
//            diffClassPath = "D:\\TestReducer\\4-23\\4-23\\04SynthesisHistory\\1682023620523\\diffClass";
        } else {
            throw new RuntimeException("Unknown System!");
        }

        DTConfiguration.setJvmDepensRoot(jvmpath);
    }

    public static void main(String[] args){
        genLatex();
    }

    public static void getOutputAfterReduced(){
        ArrayList<JvmInfo> jvmCmds = DTLoader.getInstance().loadJvms();
        File jsonFile = new File(jsonPath + DTPlatform.FILE_SEPARATOR + classFilterJson);
        List<Map<String, Object>> classes = DataGen.readJsonFile(jsonFile);

        List<String> jsonObjects = new ArrayList<>();
        for(Map<String, Object> c : classes){
            String name = c.get("classname").toString();
            String[] classPath = name.substring(0, name.lastIndexOf("_")).split("\\.");
            String tgtPath = projectPath;
            String tgtFile = name;
            if (classPath.length > 1) {
                for (int i = 0; i < classPath.length - 1; i++) {
                    String p = classPath[i];
                    tgtFile = p;
                    tgtPath = tgtPath + DTPlatform.FILE_SEPARATOR + p;
                    File file = new File(tgtPath);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                }
            }else {
                tgtFile = name.substring(0, name.lastIndexOf("_"));
            }

            DataGen.copyFile(dataOutputPath, tgtPath, name, tgtFile + ".class");

            String options = c.get("vmoptions").toString();
            ArrayList<String> optionList = new ArrayList<>(Arrays.asList(options.split(" ")));

            name = name.substring(0, name.lastIndexOf("_"));
            ClassUtil.initSootEnvWithClassPath(projectPath);
            SootClass sootClass = ClassUtil.loadClass(name);
            HashMap<String, JvmOutput> result = DTExecutor.getInstance().dtSingleClass(jvmCmds,
                    optionList, projectPath,
                    "project", sootClass.getName(), false, new String[1]);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("classname", c.get("classname").toString());
            jsonObject.put("output", result);
            String json = JSON.toJSONString(jsonObject, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteNullListAsEmpty);
            jsonObjects.add(json);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(dataOutputPath + DTPlatform.FILE_SEPARATOR + classResultJson));
            writer.write(jsonObjects.toString());
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void compareSize(){
        File jsonFile = new File(jsonPath + DTPlatform.FILE_SEPARATOR + classFilterJson);
        List<Map<String, Object>> classes = DataGen.readJsonFile(jsonFile);

        List<String> result = new ArrayList<>();
        double avg = 0d;
        for(Map<String, Object> c : classes){
            String name = c.get("classname").toString();
            File origin = new File(jsonPath + DTPlatform.FILE_SEPARATOR + name);
            File reduced = new File(dataOutputPath + DTPlatform.FILE_SEPARATOR + name);
            avg = avg + (double) reduced.length() / (double) origin.length();
            System.out.println(name + " : " + origin.length() + "  ------->  " + reduced.length() + "    " + (double) reduced.length() / (double) origin.length());
        }

        System.out.println(avg / classes.size());
    }

    public static void compareJimpleSize(){
        File jsonFile = new File(jsonPath + DTPlatform.FILE_SEPARATOR + classFilterJson);
        List<Map<String, Object>> classes = DataGen.readJsonFile(jsonFile);

        double avg = 0d;
        for(Map<String, Object> c : classes){
            String name = c.get("classname").toString();
            name = name.replace(".class", ".jimple");
            File origin = new File(jsonPath + DTPlatform.FILE_SEPARATOR + "jimple" + DTPlatform.FILE_SEPARATOR + name);
            File reduced = new File(dataOutputPath + DTPlatform.FILE_SEPARATOR + name);
            avg = avg + (double) reduced.length() / (double) origin.length();
            System.out.println(name + " : " + origin.length() + "  ------->  " + reduced.length() + "    " + (double) reduced.length() / (double) origin.length());
        }

        System.out.println(avg / classes.size());
    }

    public static void saveJimple(){
        File jsonFile = new File(jsonPath + DTPlatform.FILE_SEPARATOR + classFilterJson);
        List<Map<String, Object>> classes = DataGen.readJsonFile(jsonFile);

        for(Map<String, Object> c : classes){
            ClassUtil.initSootEnvWithClassPath("D:\\TestReducer\\reduceOrigin");
            String name = c.get("classname").toString();
            String[] classPath = name.substring(0, name.lastIndexOf(".class")).split("\\.");
            String tgtFile = name;
            if (classPath.length > 1) {
                for (int i = 0; i < classPath.length; i++) {
                    String p = classPath[i];
                    tgtFile = p;
                }
            }else {
                tgtFile = name.substring(0, name.lastIndexOf(".class"));
            }

            DataGen.copyFile(jsonPath, "D:\\TestReducer\\reduceOrigin", name, tgtFile + ".class");
            SootClass sootClass = ClassUtil.loadClass(tgtFile);
            ClassUtil.saveClass(sootClass, "D:\\TestReducer\\reduceOrigin", tgtFile);
            G.reset();
        }
    }

    public static void genLatex(){
        File jsonFile = new File(jsonPath + DTPlatform.FILE_SEPARATOR + classFilterJson);
        List<Map<String, Object>> classes = DataGen.readJsonFile(jsonFile);
        DecimalFormat df = new DecimalFormat("0.00");
        double avg = 0d;
        int idx = 46;
        for(Map<String, Object> c : classes){
            String name = c.get("classname").toString();
            String[] classPath = name.substring(0, name.lastIndexOf(".class")).split("\\.");
            String tgtFile = name;
            if (classPath.length > 1) {
                for (int i = 0; i < classPath.length; i++) {
                    String p = classPath[i];
                    tgtFile = p;
                }
            }else {
                tgtFile = name.substring(0, name.lastIndexOf(".class"));
            }

            ClassUtil.initSootEnvWithClassPath("D:\\TestReducer\\reduceOrigin");
            SootClass origin = ClassUtil.loadClass(tgtFile);
            double originUnitSum = 0;
            for(SootMethod sootMethod : origin.getMethods()){
                originUnitSum += sootMethod.retrieveActiveBody().getUnits().size();
            }
            G.reset();

            ClassUtil.initSootEnvWithClassPath(dataOutputPath + DTPlatform.FILE_SEPARATOR + "Method" + DTPlatform.FILE_SEPARATOR + "jimple");
            SootClass method = ClassUtil.loadClass(tgtFile);
            double methodUnitSum = 0;
            for(SootMethod sootMethod : method.getMethods()){
                methodUnitSum += sootMethod.retrieveActiveBody().getUnits().size();
            }
            G.reset();

            ClassUtil.initSootEnvWithClassPath(dataOutputPath + DTPlatform.FILE_SEPARATOR + "Block" + DTPlatform.FILE_SEPARATOR + "jimple");
            SootClass block = ClassUtil.loadClass(tgtFile);
            double blockUnitSum = 0;
            for(SootMethod sootMethod : block.getMethods()){
                blockUnitSum += sootMethod.retrieveActiveBody().getUnits().size();
            }
            G.reset();

            name = "c" + idx++;
            String methodPro = df.format(methodUnitSum / originUnitSum);
            String blockPro = df.format(blockUnitSum / originUnitSum);
            System.out.println(name + " & " + methodPro + " & " + blockPro + " \\\\");
        }
    }

}
