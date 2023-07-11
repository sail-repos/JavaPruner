import Configuration.PlatformInfo;
import commandLineTool.OutputAnalyzer;
import dtjvms.*;
import dtjvms.analyzer.DiffCore;
import dtjvms.analyzer.JDKAnalyzer;
import dtjvms.executor.JIT.JITExecutor;
import dtjvms.executor.JIT.JvmOutput;
import dtjvms.executor.DTExecutor;
import dtjvms.executor.ExecutorHelper;
import dtjvms.loader.DTLoader;
import org.junit.Test;
import soot.SootClass;
import utils.ClassUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 *
 *  AsmTools 命令
 *  java -cp . microbench.HelloWorld
 *  java -jar asmtools-1.0.3.jar jdis HelloWorld.class > HelloWorld.jasm
 *  //修改 JASM 文件
 *  java -jar asmtools-1.0.3.jar jasm HelloWorld.jasm
 *
 *  String cmd = "java -jar asmtools.jar jdis Foo.class > Foo.jasm";
 *  System.out.println("Main");
 */
public class ClassValidate {

    public static String jvmpath = "";
    public static String junit = "lib/junit-4.12.jar:lib/hamcrest-core-1.3.jar:lib/mockito-all-1.10.19.jar";
    public static String AsmTool = "lib/asmtools-1.0.3.jar";
    public static String classPath = "../BugContainer/target/classes";
    public static String jasmPath = "../BugContainer/JasmTransfor";
    public static String byteCodePath = "../BugContainer/JasmTransfor";

    public boolean isJunit = false;

    public static String className = "StackMapTableTest";
//    public static String className = "ChecksumDiff02";
//    public static String className = "ChecksumDiff01";

    static {

        if (PlatformInfo.isMac()){
            jvmpath = "/Users/yingquanzhao/Workspace/JVM/JITFuzzing/01JVMS";
        } else if (PlatformInfo.isLinux()){

//            jvmpath = "/media/quan/ZYQ/JDK";
            jvmpath = "/home/quan/Workspace/DTJVM/JVM-depens";
        } else if (PlatformInfo.isWin()){

            //TODO add win jvm path
            junit = "lib\\junit-4.12.jar;lib\\hamcrest-core-1.3.jar;lib\\mockito-all-1.10.19.jar;lib\\commons-validator-1.3.1.jar";
            AsmTool = "lib\\asmtools-1.0.3.jar";
            classPath = "D:\\LearnFile\\JVM\\Projects\\ClassValidate\\BugContainer\\target\\classes";
            jvmpath = "..\\01JVMs";
            jasmPath = "..\\BugContainer\\JasmTransfor";
            byteCodePath = "..\\BugContainer\\JasmTransfor";
        } else {
            throw new RuntimeException("Unknown System!");
        }
    }

    @Test
    public void covertClassToJimple() throws IOException {

        ClassUtils.initSootEnvWithClassPath(classPath);
        SootClass sootclass = ClassUtils.loadClass(className);
        String jasmFilePath = jasmPath + DTPlatform.FILE_SEPARATOR +
                "origin" + DTPlatform.FILE_SEPARATOR;

        ClassUtils.saveClassToPath(sootclass, jasmFilePath);
    }

    /**
     * java -jar lib/asmtools-1.0.3.jar jdis target/classes/microbench/ISME.src1.Outer.class
     * 将CLASS文件转换成 JASM 文件， 保存至 mutate 文件夹中，用于修改用
     * @throws IOException
     */
    @Test
    public void covertClassToJasm() throws IOException {

        String tool = "jdis";

        String classFilePath = classPath + DTPlatform.FILE_SEPARATOR +
                className.replace(".", DTPlatform.FILE_SEPARATOR) + ".class";
        String jasmFilePath = jasmPath + DTPlatform.FILE_SEPARATOR +
                "mutate" + DTPlatform.FILE_SEPARATOR +
                className + ".jasm";

        LinkedList<String> cmdArgs = new LinkedList<>();
        Collections.addAll(cmdArgs,
                "java",
                "-jar",
                AsmTool,
                tool,
                classFilePath
        );

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs);
        OutputAnalyzer analyzer = new OutputAnalyzer(processBuilder.start());
        System.err.println(analyzer.getStderr());
        analyzer.outputToPath(jasmFilePath);

        saveOriginClassJasm();
    }

    /**
     * 将 mutate 文件夹中修改后的 jasm 文件转换成 class 文件，用于差异测试
     * @throws IOException
     */
    @Test
    public void covertJasmToClass() throws IOException {

        String tool = "jasm";

        String jasmFilePath = jasmPath + DTPlatform.FILE_SEPARATOR +
                "mutate" + DTPlatform.FILE_SEPARATOR +
                className + ".jasm";

        LinkedList<String> cmdArgs = new LinkedList<>();
        Collections.addAll(cmdArgs,
                "java",
                "-jar",
                AsmTool,
                tool,
                jasmFilePath,
                "-d",
                classPath
        );

        System.out.println(cmdArgs);
        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs);
        OutputAnalyzer analyzer = new OutputAnalyzer(processBuilder.start());
        System.err.println(analyzer.getOutput());

        saveMutateClassToBytecode();
        DTMutateClass();
    }

    /**
     * 差异测试
     * @throws IOException
     */
    @Test
    public void DTMutateClass() throws IOException {

        boolean debug = true;
        /**
         * 01 初始化运行环境、JVM
         */
        DTConfiguration.setJvmDepensRoot(jvmpath);

        System.out.println(DTPlatform.getInstance());
        ArrayList<JvmInfo> jvmCmds = DTLoader.getInstance().loadJvms();

        for (JvmInfo jvmCmd : jvmCmds) {
            System.out.println(jvmCmd);
        }

//        if(isJunit){
            if (PlatformInfo.isWin()){
                classPath = classPath + ";" + junit;
            } else {
                classPath = classPath + ":" + junit;
            }
//        }

        if (debug){

            HashMap<String, JvmOutput> results = new HashMap<>();
            for (JvmInfo cmd : jvmCmds) {

                System.out.println("= = = = = = = = = = = = = " + cmd.getJvmId() + " = = = = = = = = = = = = = = = = = = ");
                String javaCmd = cmd.getJavaCmd();
                String cmdExecutor = ExecutorHelper.assembleJavaCmd(javaCmd,null, classPath, className, isJunit ,new String[]{});
                System.out.println(cmdExecutor);
                String jvmId = cmd.getJvmId() != null ? cmd.getJvmId() : cmd.getFolderName();

                Thread test = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        JITExecutor.getInstance().execute(cmdExecutor);
                    }
                });
                test.start();
                try {
                    TimeUnit.SECONDS.timedJoin(test, DTConfiguration.CLASS_MAX_RUNTIME);
                    JITExecutor.getInstance().shutDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (JITExecutor.getInstance().getCurrentOutput() != null){
                    System.out.println(JITExecutor.getInstance().getCurrentOutput().getOutput());
                    results.put(jvmId, JITExecutor.getInstance().getCurrentOutput());
                } else {
                    results.put(jvmId, new JvmOutput(""));
                }
            }

//            DiffCore diff = JDKAnalyzer.getInstance().analysis(className, results);
//            if (diff != null){
//
//                ExecutorHelper.logJvmOutput("Main", className, diff ,results);
//            }

//            if (JDKAnalyzer.getInstance().analysis(className, results)){
//                ExecutorHelper.logJvmOutput("Main", className, results);
//            }
        } else {

            for (JvmInfo cmd : jvmCmds) {

                System.out.println("= = = = = = = = = = = = = " + cmd.getJvmId() + " = = = = = = = = = = = = = = = = = = ");
                String javaCmd = cmd.getJavaCmd();
                String cmdExecutor = ExecutorHelper.assembleJavaCmd(javaCmd,null, classPath, className, isJunit ,new String[]{});
                System.out.println(cmdExecutor);
                DTExecutor.getInstance().execute(cmdExecutor);
            }
        }
    }

    /**
     * 保存变异后CLASS文件对应的字节码文件，至mutate 文件夹中，用于对比
     * @throws IOException
     */
    @Test
    public void saveMutateClassToBytecode() throws IOException {

        String tool = "javap";
        String classFilePath = classPath + DTPlatform.FILE_SEPARATOR +
                className.replace(".", DTPlatform.FILE_SEPARATOR) + ".class";

        String byteCodeFilePath = byteCodePath + DTPlatform.FILE_SEPARATOR +
                "mutate" + DTPlatform.FILE_SEPARATOR +
                className + ".txt";
        LinkedList<String> cmdArgs = new LinkedList<>();
        Collections.addAll(cmdArgs,
                tool,
                "-v",
                "-p",
                classFilePath
        );

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs);
        OutputAnalyzer analyzer = new OutputAnalyzer(processBuilder.start());
        System.err.println(analyzer.getStderr());
        analyzer.outputToPath(byteCodeFilePath);
    }

    /**
     * 保存原始的字节码文件， 保存至origin文件夹中，用于比较用
     * @throws IOException
     */
    @Test
    public void saveOriginClassBytecode() throws IOException {

        String tool = "javap";
        String classFilePath = classPath + DTPlatform.FILE_SEPARATOR +
                className.replace(".", DTPlatform.FILE_SEPARATOR) + ".class";

        String byteCodeFilePath = byteCodePath + DTPlatform.FILE_SEPARATOR +
                "origin" + DTPlatform.FILE_SEPARATOR +
                className + ".txt";
        LinkedList<String> cmdArgs = new LinkedList<>();
        Collections.addAll(cmdArgs,
                tool,
                "-v",
                "-p",
                classFilePath
        );

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs);
        OutputAnalyzer analyzer = new OutputAnalyzer(processBuilder.start());
        System.err.println(analyzer.getStderr());
        analyzer.outputToPath(byteCodeFilePath);
    }

    /**
     * 保存原始的Jasm文件， 保存至origin文件夹中，用于比较用
     * @throws IOException
     */
    @Test
    public void saveOriginClassJasm() throws IOException {

        String tool = "jdis";

        String classFilePath = classPath + DTPlatform.FILE_SEPARATOR +
                className.replace(".", DTPlatform.FILE_SEPARATOR) + ".class";

        String jasmFilePath = jasmPath + DTPlatform.FILE_SEPARATOR +
                "origin" + DTPlatform.FILE_SEPARATOR +
                className + ".jasm";

        LinkedList<String> cmdArgs = new LinkedList<>();
        Collections.addAll(cmdArgs,
                "java",
                "-jar",
                AsmTool,
                tool,
                classFilePath
        );

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs);
        OutputAnalyzer analyzer = new OutputAnalyzer(processBuilder.start());
        System.err.println(analyzer.getStderr());
        analyzer.outputToPath(jasmFilePath);

        saveOriginClassBytecode();
    }
}
