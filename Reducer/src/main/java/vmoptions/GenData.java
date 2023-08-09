package vmoptions;

import Utils.ClassUtil;
import com.alibaba.fastjson.JSONObject;
import dtjvms.DTConfiguration;
import dtjvms.DTPlatform;
import dtjvms.DTProperties;
import dtjvms.JvmInfo;
import dtjvms.analyzer.*;
import dtjvms.executor.DTExecutor;
import dtjvms.executor.JIT.JvmOutput;
import dtjvms.loader.DTLoader;
import soot.G;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GenData {

    private static ArrayList<JvmInfo> jvmCmds;

    private static String ROOT;
    private static String REDUCE_CLASS_PATH;
    private static String REDUCE_CHECKDIR;

    public static void main(String[] args) throws IOException {
        DTProperties dtProperties = DTProperties.getInstance();
        ROOT = dtProperties.getProperty(DTProperties.TARGET_PROJECTS_KEY);
        REDUCE_CHECKDIR = ROOT + "\\" + dtProperties.getProperty(DTProperties.PROJECT_REDUCE_DIR);
        REDUCE_CLASS_PATH = ROOT + "\\" + dtProperties.getProperty(DTProperties.PROJECT_REDUCE_DIR);

        DTConfiguration.setJvmDepensRoot(ROOT + DTPlatform.FILE_SEPARATOR + "01JVMS");
        DTConfiguration.setProjectDepensRoot(ROOT + DTPlatform.FILE_SEPARATOR + "02Benchmarks");

        jvmCmds = DTLoader.getInstance().loadJvms();

        String resource = System.getProperty("user.dir") + DTPlatform.FILE_SEPARATOR + "reduceProperties" + DTPlatform.FILE_SEPARATOR;
        List<Map<String, String>> reduceClasses = getJsonString(resource + "reduceClass.json");

        FilterChain filterChain = new FilterChain();

        filterChain.addFilter(new WarningFilter());
        filterChain.addFilter(new JunitFilter());
        filterChain.addFilter(new StdErrFilter());
        filterChain.addFilter(new DefErrFilter());

        File file = new File("ReduceProperties\\dataResult.log");

        for(Map<String, String> classFile : reduceClasses) {
            G.reset();
            ClassUtil.initSootEnvWithClassPath(REDUCE_CLASS_PATH);
            String mainClass = classFile.get("className");
            ArrayList<String> options = new ArrayList<>(Arrays.asList(classFile.get("options").split(" ")));
            HashMap<String, JvmOutput> result = DTExecutor.getInstance().dtSingleClass(jvmCmds, options, REDUCE_CLASS_PATH, "project", mainClass, false, new String[1]);
            filterChain.startFilter(result);
            String resultString = mainClass + " :: \n";
            for(String k : result.keySet()){
                JvmOutput jvmOutput = result.get(k);
                resultString = resultString + k + ":" + jvmOutput.getExceptions() + " " + jvmOutput.getErrors() + " " + jvmOutput.getFailures() + " " + jvmOutput.getFEEInfo() + "\n";
            }

//            FileUtils.writeStringToFile(file, resultString, "UTF-8", true);
        }
    }

    private static List<Map<String, String>> getJsonString(String filepath) {
        StringBuffer sb = new StringBuffer();
        try {
            Reader reader = new InputStreamReader(new FileInputStream(filepath), StandardCharsets.UTF_8);
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
