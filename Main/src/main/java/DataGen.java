import JimpleMixer.blocks.BlockInfo;
import JimpleMixer.blocks.LoopStmtBlockInfo;
import JimpleMixer.blocks.MyBlockContainer;
import Reducer.BlockReducer;
import Reducer.BlockStruct;
import Reducer.CFGStructParser;
import Utils.ClassUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import Configuration.PlatformInfo;
import com.alibaba.fastjson.serializer.SerializerFeature;
import dtjvms.DTConfiguration;
import dtjvms.DTPlatform;
import dtjvms.JvmInfo;
import dtjvms.ProjectInfo;
import dtjvms.executor.DTExecutor;
import dtjvms.executor.JIT.JvmOutput;
import dtjvms.loader.DTLoader;
import polyglot.ast.ArrayAccess;
import polyglot.ast.Do;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.*;

public class DataGen {

    public static String jvmpath = "";
    public static String dataSrcPath = "";
    public static String projectPath = "./Benchmarks";
    public static String targetProjectName = "";
    public static String diffClassPath = "";
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
            dataSrcPath = "D:\\master\\JVM-Testing\\Reduce\\dataset";
//            projectPath = "D:\\TestReducer\\HotspotTests-Java-gtc\\HotspotTests-Java\\out\\production\\HotspotTests-Java";
//            diffClassPath = "D:\\TestReducer\\4-23\\4-23\\04SynthesisHistory\\1682023620523\\diffClass";
        } else {
            throw new RuntimeException("Unknown System!");
        }

        DTConfiguration.setJvmDepensRoot(jvmpath);
    }

    public static void main(String[] args){
//        getOrigin();
//        getDataLatex();
//        getRQ1Result();
        getRQ2Result();
//        editInfo();
    }

    public static void editInfo(){
        for(int i = 1; i <= 50; i++){
            String cid = "c" + i;
            String tgtDir = dataSrcPath  + DTPlatform.FILE_SEPARATOR + "Test-Classes" + DTPlatform.FILE_SEPARATOR + cid + DTPlatform.FILE_SEPARATOR + "classInfo.json";
            File file = new File(tgtDir);
            Map<String, Object> map = readJsonMap(file);

            JSONObject jsonObject = new JSONObject(map);
            jsonObject.put("filename", map.get("classname").toString());

            File tgt = new File(tgtDir);
            if(!(tgt.exists())){
                tgt.mkdir();
            }
            //写入操作
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(tgtDir));
                bw.write(jsonObject.toString());//转化成字符串再写
                bw.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static void getOrigin(){
        for(int i = 36; i <= 36; i++){
            String cid = "c" + i;
            File classInfo = new File(dataSrcPath + DTPlatform.FILE_SEPARATOR + "Test-Classes" + DTPlatform.FILE_SEPARATOR + cid + DTPlatform.FILE_SEPARATOR + "classInfo.json");
            Map<String, Object> info = DataGen.readJsonMap(classInfo);
            String classname = info.get("classname").toString();
            String classpath = info.get("classpath").toString();
            String project = info.get("project").toString();
            String filename = info.get("filename").toString();
            String projectPath = dataSrcPath + DTPlatform.FILE_SEPARATOR + "Projects" + DTPlatform.FILE_SEPARATOR + project;
            String classPath = classpath;
            String fullName = classpath;
            if(classPath.contains(".")){
                classPath = classPath.replaceAll("\\.", "\\\\");
                fullName = fullName + "." + classname;
            }else {
                fullName = fullName + classname;
            }
            String srcPath = dataSrcPath + DTPlatform.FILE_SEPARATOR + "Test-Classes" + DTPlatform.FILE_SEPARATOR + cid;
            String tgtPath = projectPath + DTPlatform.FILE_SEPARATOR + classPath;
            copyFile(srcPath, tgtPath, filename  +  ".class",  classname + ".class");
            ClassUtil.initSootEnvWithClassPath(projectPath);
            SootClass sootClass = ClassUtil.loadClass(fullName);

            int blocksum = 0;
            int unitnum = 0;

            for(SootMethod sootMethod : sootClass.getMethods()){
                Body body = sootMethod.retrieveActiveBody();
//                CFGStructParser cfgStructParser = new CFGStructParser(new ArrayList<BlockInfo>(), body);
//                cfgStructParser.parse();
//
//                blocksum += cfgStructParser.structList.size();
                List<BlockInfo > blockInfos = MyBlockContainer.getBlockFromMethod(sootClass, sootMethod);
                for(BlockInfo blockInfo : blockInfos){
                    if(blockInfo instanceof LoopStmtBlockInfo){
                        continue;
                    }

                    blocksum ++;
                }

                for(Unit u : body.getUnits()){
                    unitnum ++;
                }
            }

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("id", cid);
            jsonObj.put("name", classname);
            jsonObj.put("filename", filename);
            jsonObj.put("methodnum", sootClass.getMethods().size());
            jsonObj.put("blocknum", blocksum);
            jsonObj.put("stmtnum", unitnum);

            if(i < 21){
                jsonObj.put("src", "JavaTailor");
            }else {
                jsonObj.put("src", "VECT");
            }

            if(i < 41){
                jsonObj.put("type", "Exception/Crash");
            }else {
                jsonObj.put("type", "Checksum不一致");
            }

            writeJson(jsonObj, cid);
            System.out.println(cid);
        }

    }

    public static void getDataLatex(){
        for(int i = 1; i <= 50; i++){
            String cid = "c" + i;
            File file = new File(dataSrcPath  + DTPlatform.FILE_SEPARATOR + "Results" + DTPlatform.FILE_SEPARATOR + "Origin" + DTPlatform.FILE_SEPARATOR + cid + DTPlatform.FILE_SEPARATOR + "classInfo.json");
            Map<String, Object> map = readJsonMap(file);
            String name = map.get("filename").toString();
            String stmtnum = map.get("stmtnum").toString();
            String blocknum = map.get("blocknum").toString();
            String methodnum = map.get("methodnum").toString();
            String src = map.get("src").toString();
            String type = map.get("type").toString();

            System.out.println(cid + " & " + name.replace("_", "\\_") + " & " + stmtnum + " & " + blocknum + " & " + methodnum + " & " + src + " & " + type + " \\\\ ");
        }
    }

    public static void getRQ1Result(){

        DecimalFormat decimalFormat = new DecimalFormat("0.0000000");

        String s1 = "";
        String s2 = "";

        for(int i = 1; i <= 50; i++){
            String cid = "c" + i;
            File origin = new File(dataSrcPath  + DTPlatform.FILE_SEPARATOR + "Results" + DTPlatform.FILE_SEPARATOR + "Origin" + DTPlatform.FILE_SEPARATOR + cid + DTPlatform.FILE_SEPARATOR + "classInfo.json");
            Map<String, Object> map = readJsonMap(origin);
            String name = map.get("name").toString();
            String stmtnum = map.get("stmtnum").toString();

            File block = new File(dataSrcPath  + DTPlatform.FILE_SEPARATOR + "Results" + DTPlatform.FILE_SEPARATOR + "Block Level" + DTPlatform.FILE_SEPARATOR + cid + DTPlatform.FILE_SEPARATOR + "result.json");
            Map<String, Object> rmap = readJsonMap(block);
            String bstmtnum = rmap.get("stmtnum").toString();

            File method = new File(dataSrcPath  + DTPlatform.FILE_SEPARATOR + "Results" + DTPlatform.FILE_SEPARATOR + "Method Level" + DTPlatform.FILE_SEPARATOR + cid + DTPlatform.FILE_SEPARATOR + "result.json");
            Map<String, Object> mmap = readJsonMap(method);
            String mstmtnum = mmap.get("stmtnum").toString();

            String bp = decimalFormat.format(Double.parseDouble(bstmtnum) / Double.parseDouble(stmtnum));
            String mp = decimalFormat.format(Double.parseDouble(mstmtnum) / Double.parseDouble(stmtnum));

            System.out.println(cid + " & " + mp + " & " + bp + " \\\\ ");

//            s1 = s1 + "," + mp;
//            s2 = s2 + "," + bp;
        }

        System.out.println(s1);
        System.out.println(s2);
    }

    public static void getRQ2Result(){
        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        for(int i = 1; i <= 50; i++){
            String cid = "c" + i;

            File block = new File(dataSrcPath  + DTPlatform.FILE_SEPARATOR + "Results" + DTPlatform.FILE_SEPARATOR + "Block Level" + DTPlatform.FILE_SEPARATOR + cid + DTPlatform.FILE_SEPARATOR + "result.json");
            Map<String, Object> rmap = readJsonMap(block);
            double bcon = Double.parseDouble(rmap.get("consuming").toString());

            File method = new File(dataSrcPath  + DTPlatform.FILE_SEPARATOR + "Results" + DTPlatform.FILE_SEPARATOR + "Non Preprocessing" + DTPlatform.FILE_SEPARATOR + cid + DTPlatform.FILE_SEPARATOR + "result.json");
            Map<String, Object> mmap = readJsonMap(method);
            double ncon = Double.parseDouble(mmap.get("consuming").toString());

            double delta = ncon - bcon;

            System.out.println(cid + " & " + (int)ncon + " & " + (int)bcon + " & " + (int)delta + " & " + decimalFormat.format((delta / ncon) * 100) + " \\\\ ");
        }
    }

    public static void copyFile(String srcPath, String tgtPath, String srcFile, String tgtFile){
        File src = new File(srcPath + DTPlatform.FILE_SEPARATOR + srcFile);
        File tgt = new File(tgtPath + DTPlatform.FILE_SEPARATOR + tgtFile);
        try {
            Files.copy(src.toPath(), tgt.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void writeJson(JSONObject jsonObj, String cid){

        String tgtDir = dataSrcPath  + DTPlatform.FILE_SEPARATOR + "Results" + DTPlatform.FILE_SEPARATOR + "Origin" + DTPlatform.FILE_SEPARATOR + cid;
        File tgt = new File(tgtDir);
        if(!(tgt.exists())){
            tgt.mkdir();
        }
        //写入操作
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(tgtDir + DTPlatform.FILE_SEPARATOR + "classInfo.json"));
            bw.write(jsonObj.toString());//转化成字符串再写
            bw.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<Map<String, Object>> readJsonFile(File jsonFile){
        StringBuffer sb = new StringBuffer();
        try {
            Reader reader = new InputStreamReader(Files.newInputStream(jsonFile.toPath()), StandardCharsets.UTF_8);
            int ch;
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List result = JSONObject.parseObject(sb.toString().replaceAll("\r\n", ""), List.class);
        return (List<Map<String, Object>>) result;
    }

    public static Map<String, Object> readJsonMap(File jsonFile){
        StringBuffer sb = new StringBuffer();
        try {
            Reader reader = new InputStreamReader(Files.newInputStream(jsonFile.toPath()), StandardCharsets.UTF_8);
            int ch;
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> result = JSONObject.parseObject(sb.toString().replaceAll("\r\n", ""), Map.class);
        return result;
    }

    public static void getDataJson(){
        File dir = new File(dataSrcPath);
        File[] files = dir.listFiles();
        List<JSONObject> jsonList = new ArrayList<>();
        for(File f : files){
            if(f.getName().endsWith(".class")) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("classname", f.getName());
                jsonObject.put("vmoption", "");
                jsonList.add(jsonObject);
            }
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(dataSrcPath + DTPlatform.FILE_SEPARATOR + classJson));
            writer.write(jsonList.toString());
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void FilterData(){
        File resultFile = new File(dataSrcPath + DTPlatform.FILE_SEPARATOR + classResultJson);
        List<Map<String, Object>> list = readJsonFile(resultFile);
        Set<String> legalClasses = new HashSet<>();
        for(Map<String, Object> m : list){
            legalClasses.add(m.get("classname").toString());
        }

        List<Map<String, Object>> originList = readJsonFile(new File(dataSrcPath + DTPlatform.FILE_SEPARATOR + classJson));
        List<String> jsonList = new ArrayList<>();
        for(Map<String, Object> c : originList){
            String name = c.get("classname").toString();
            if(legalClasses.contains(name)){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("classname", name);
                jsonObject.put("vmoptions", c.get("vmoptions") == null ? "" : c.get("vmoptions"));
                String json = JSON.toJSONString(jsonObject, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteNullListAsEmpty);
                jsonList.add(json);
            }
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(dataSrcPath + DTPlatform.FILE_SEPARATOR + classFilterJson));
            writer.write(jsonList.toString());
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
