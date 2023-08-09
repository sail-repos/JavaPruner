package Reducer;

import Utils.ClassUtil;
import Utils.FileUtil;
import com.alibaba.fastjson.JSONObject;
import dtjvms.DTPlatform;
import dtjvms.JvmInfo;
import dtjvms.executor.DTExecutor;
import dtjvms.executor.JIT.JvmOutput;
import soot.G;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ReduceChain {

    public Reducer headReducer;
    public Reducer currentReducer;

    public ClassInfo classInfo;

    public String dataPath;

    public HashMap<String, JvmOutput> originResult;

    public ArrayList<JvmInfo> jvmCmds;

    public static String executePath;

    public SootClass sootClass;

    public Checker checker;

    public static String tmp = "D:\\TestReducer\\tmp";

    public ReduceChain(String dataPath, ClassInfo classInfo, ArrayList<JvmInfo> jvmCmds){
        this.classInfo = classInfo;
        this.dataPath = dataPath;
        this.jvmCmds = jvmCmds;
        checker = new Checker(jvmCmds);
    }

    public void addReducer(Reducer reducer){
        reducer.checker = checker;
        if(headReducer == null){
            headReducer = reducer;
            currentReducer = reducer;
        }else {
            currentReducer.setNextReducer(reducer);
            currentReducer = reducer;
        }
    }

    public void startReduce(){
        pretreatment();
        ArrayList<String> optionList = new ArrayList<>(Arrays.asList(classInfo.vmoptions.split(" ")));
        originResult = DTExecutor.getInstance().dtSingleClass(jvmCmds,
                optionList, executePath,
                "project", sootClass.getName(), false, new String[1]);
        currentReducer = headReducer;
        Long t1 = System.currentTimeMillis();
        while(currentReducer != null){
            if(!(currentReducer.doReduce(sootClass, originResult, optionList, classInfo.testtype))){
                reload();
            }else {
                ClassUtil.saveClass(sootClass, tmp, sootClass.getShortName());
            }
            currentReducer = currentReducer.nextReducer;
        }
        Long t2 = System.currentTimeMillis();

        String outputDir = dataPath + DTPlatform.FILE_SEPARATOR + "Results" + DTPlatform.FILE_SEPARATOR + "Method Level" + DTPlatform.FILE_SEPARATOR + classInfo.id;
        File dir = new File(outputDir);
        if(!(dir.exists())){
            dir.mkdir();
        }
        writeJson(outputDir, t2 - t1);
        ClassUtil.saveClass(sootClass, outputDir, sootClass.getName());
    }

    public void pretreatment(){
        String projectPath = dataPath + DTPlatform.FILE_SEPARATOR + "Projects" + DTPlatform.FILE_SEPARATOR + classInfo.project;
        String classPath = classInfo.classpath;
        String fullName = classInfo.classpath;
        if(classPath.contains(".")){
            classPath = classPath.replaceAll("\\.", "\\\\");
            fullName = fullName + "." + classInfo.classname;
        }else {
            fullName = fullName + classInfo.classname;
        }
        String srcPath = dataPath + DTPlatform.FILE_SEPARATOR + "Test-Classes" + DTPlatform.FILE_SEPARATOR + classInfo.id;
        String tgtPath = projectPath + DTPlatform.FILE_SEPARATOR + classPath;
        executePath = projectPath;
        FileUtil.copyFile(srcPath, tgtPath, classInfo.filename  +  ".class",  classInfo.classname + ".class");
        ClassUtil.initSootEnvWithClassPath(projectPath);
        sootClass = ClassUtil.loadClass(fullName);
        ClassUtil.saveClass(sootClass, tmp, sootClass.getShortName());
        checker.setDir(tgtPath);
        checker.setProjectPath(executePath);
    }

    public void reload(){
        System.out.println("reload");
        String fullName = sootClass.getName();
        FileUtil.copyFile(tmp, checker.getDir(), sootClass.getShortName() + ".class", sootClass.getShortName() + ".class");
        G.reset();
        ClassUtil.initSootEnvWithClassPath(executePath);
        sootClass = ClassUtil.loadClass(fullName);
    }

    public void writeJson(String outputDir, Long consume){

        int num = 0;
        for(SootMethod m : sootClass.getMethods()){
            for(Unit u : m.retrieveActiveBody().getUnits()){
                num ++;
            }
        }

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("stmtnum", num);
        jsonObj.put("consuming", consume/1000);
        //写入操作
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(outputDir + DTPlatform.FILE_SEPARATOR + "result.json"));
            bw.write(jsonObj.toString());//转化成字符串再写
            bw.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
