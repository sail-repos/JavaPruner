import Configuration.PlatformInfo;
import commandLineTool.OutputAnalyzer;
import dtjvms.DTConfiguration;
import dtjvms.DTPlatform;
import dtjvms.JvmInfo;
import dtjvms.ProjectInfo;
import dtjvms.analyzer.DiffCore;
import dtjvms.analyzer.JDKAnalyzer;
import dtjvms.executor.JIT.JITExecutor;
import dtjvms.executor.JIT.JvmOutput;
import dtjvms.executor.DTExecutor;
import dtjvms.executor.ExecutorHelper;
import dtjvms.loader.DTLoader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * 测试Project中的对应类
 *
 * Step：
 *      1、设置JVM路径：{@link #jvmpath}
 *      2、设置Project路径 ：{@link #originProjPath}{@link #mutateProjPath}
 *      3、设置变异类所在的变异历史路径 ：{@link #mutationHistoryPath}
 *      4、设置测试类名称 ： {@link #className}
 *
 * 执行：
 *      1、首先将mutationHistoryPath中的类转为jasm文件，保存至 JasmTransfor {@link #jasmDir} 文件夹中，执行{@link #covertMutateClassToJasm}函数
 *      2、手动修改 JasmTransfor{@link #jasmDir} 文件夹中的jasm文件
 *      3、将修改后的jasm文件再导出为class文件，保存至project对应的目录中，执行{@link #covertJasmToClass}函数
 *      4、测试修改后的CLASS文件，执行{@link #loadJvmAndProject}函数
 */
public class ProjectValidate {

    public static String jvmpath = "";
    public static String originProjPath = "../Benchmarks/Origin";
    public static String mutateProjPath = "../Benchmarks/Mutate";

    public static String mutationProjectPath = "/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/02Benchmarks";
    public static String mutationProvideProjS = "HotspotTests-Java";
//    public static String mutationProvideProjS = "Templates";
    public static boolean crossProject = false;

    public static String mutationHistoryPath = "";

    public static String AsmTool = "lib/asmtools-1.0.3.jar";
    public static String jasmDir = "../BugContainer/JasmTransfor";
    public static String byteCodePath = "../BugContainer/JasmTransfor";

//    public static String projectName = "HotspotTests-Java";
    public static String projectName = "Templates";
    public static boolean isJunit = false;

    public static String className = "demo4_0401@1680735120807";


    static {

        if (!projectName.equals(mutationProvideProjS)){
            crossProject = true;
        }

        if (PlatformInfo.isMac()){
            jvmpath = "/Users/yingquanzhao/Workspace/JVM/02JIT/Projects/JITFuzzing/01JVMS";
            originProjPath = "/Users/yingquanzhao/Workspace/JVM/02JIT/Projects/JITFuzzing/02Benchmarks";
            mutateProjPath = "/Users/yingquanzhao/Workspace/JVM/02JIT/Projects/JITFuzzing/sootOutput";
            mutationProjectPath = "/Users/yingquanzhao/Workspace/JVM/02JIT/Projects/JITFuzzing/02Benchmarks";
            mutationHistoryPath = "/Users/yingquanzhao/Workspace/JVM/02JIT/Projects/JITFuzzing/03results/1680734219/diffClasses";
//            mutationHistoryPath = "/Users/yingquanzhao/Workspace/JVM/05Server/1673653121/diffClasses";
        } else if (PlatformInfo.isLinux()){

//            jvmpath = "/media/quan/ZYQ/JDK";
            jvmpath = "/home/quan/Workspace/DTJVM/JVM-depens";
            originProjPath = "/home/quan/Workspace/CFMutation/02Benchmarks";
            mutateProjPath = "/home/quan/Workspace/CFMutation/sootOutput";
            mutationProjectPath = "/home/quan/Workspace/CFMutation/02Benchmarks";
            mutationHistoryPath = "/mnt/hgfs/JVM/results/2020-7-13/history/1625776434/randomSeed-1";
        } else if (PlatformInfo.isWin()){

            //TODO add win jvm path
            jvmpath = "..\\01JVMs";
            jasmDir = "..\\BugContainer\\JasmTransfor";
            byteCodePath = "..\\BugContainer\\JasmTransfor";
            originProjPath = "..\\Benchmarks\\Origin";
            mutateProjPath = "..\\Benchmarks\\Mutate";
            mutationProjectPath = "..\\Benchmarks\\Origin";
//            mutationHistoryPath = "..\\02results\\2020-7-5\\history\\1625520146\\randomSeed-1";
            mutationHistoryPath = "D:\\LearnFile\\JVM\\Results\\2020-7-17\\history\\1626476268\\randomSeed-1";
        } else {
            throw new RuntimeException("Unknown System!");
        }
    }

    /**
     * 为了对比，将原始的class文件转换成jimple文件
     * @throws IOException
     */
    @Test
    public void covertOriginClassToJasm() throws IOException {

        String tool = "jdis";

        ProjectInfo originProject = DTLoader.getInstance().loadTargetProjectWithGivenPath(originProjPath, projectName, null, false);

        String orginClassname = className.substring(0, className.lastIndexOf("_"));
        String cpath = orginClassname.replace(".", DTPlatform.FILE_SEPARATOR) + ".class";

        String classPath;
        if (isJunit){
            classPath = originProject.getTestClassPath() + DTPlatform.FILE_SEPARATOR + cpath;
        } else {
            classPath = originProject.getSrcClassPath() + DTPlatform.FILE_SEPARATOR + cpath;
        }

        String jasmPath = jasmDir + DTPlatform.FILE_SEPARATOR +
                "origin" + DTPlatform.FILE_SEPARATOR +
                className + ".jasm";

        System.out.println(jasmPath);
        LinkedList<String> cmdArgs = new LinkedList<>();
        Collections.addAll(cmdArgs,
                "java",
                "-jar",
                AsmTool,
                tool,
                classPath
        );

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs);
        OutputAnalyzer analyzer = new OutputAnalyzer(processBuilder.start());
        System.err.println(analyzer.getStderr());
        analyzer.outputToPath(jasmPath);
    }

    /**
     * 将变异的类文件直接拷贝到对应目录中，而不需要先转换成jasm再转换成class
     */
    @Test
    public void copyMutateClassToSrcDirectly(){

        String packageName = className.substring(0, className.lastIndexOf("_"));
        String mutateClassPath = mutationHistoryPath + DTPlatform.FILE_SEPARATOR +
                packageName + DTPlatform.FILE_SEPARATOR +
                className + ".class";

        ProjectInfo mutateProject = DTLoader.getInstance().loadTargetProjectWithGivenPath(mutateProjPath, projectName, null, false);

        String targetFilePath;

        if (isJunit){
            targetFilePath = mutateProject.getTestClassPath();
        } else {
            targetFilePath = mutateProject.getSrcClassPath();
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

        //print cg
//        CGGenerator.printCG(packageName, mutateProject);
        //print cfg
//        CFGGenerator.printCFG(packageName, mutateProject);
        loadJvmAndProject();
    }

    @Test
    public void covertMutateClassToJasm() throws IOException {

        String tool = "jdis";

        String packageName = className.substring(0, className.lastIndexOf("_"));
        String classPath = mutationHistoryPath + DTPlatform.FILE_SEPARATOR +
                packageName + DTPlatform.FILE_SEPARATOR +
                className + ".class";
        String jasmPath = jasmDir + DTPlatform.FILE_SEPARATOR +
                "mutate" + DTPlatform.FILE_SEPARATOR +
                className + ".jasm";

        System.out.println(jasmPath);
        LinkedList<String> cmdArgs = new LinkedList<>();
        Collections.addAll(cmdArgs,
                "java",
                "-jar",
                AsmTool,
                tool,
                classPath
        );
        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs);
        OutputAnalyzer analyzer = new OutputAnalyzer(processBuilder.start());
        System.err.println(analyzer.getStderr());
        analyzer.outputToPath(jasmPath);
    }


    @Test
    public void covertJasmToClass() throws IOException {

        String tool = "jasm";

        String jasmPath = jasmDir + DTPlatform.FILE_SEPARATOR +
                "mutate" + DTPlatform.FILE_SEPARATOR +
                className + ".jasm";

        ProjectInfo mutateProject = DTLoader.getInstance().loadTargetProjectWithGivenPath(mutateProjPath, projectName, null, false);

        String targetFilePath;

        if (isJunit){
            targetFilePath = mutateProject.getTestClassPath();
        } else {
            targetFilePath = mutateProject.getSrcClassPath();
        }

//        if (className.contains(".")){
//            String cpath = className.substring(0, className.lastIndexOf("."));
//            cpath = cpath.replace(".", "/");
//            targetFilePath = mutateProject.getSrcClassPath() + DTPlatform.FILE_SEPARATOR + cpath;
//        }

        LinkedList<String> cmdArgs = new LinkedList<>();
        Collections.addAll(cmdArgs,
                "java",
                "-jar",
                AsmTool,
                tool,
                jasmPath,
                "-d",
                targetFilePath
        );

        System.out.println(cmdArgs);
        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs);
        OutputAnalyzer analyzer = new OutputAnalyzer(processBuilder.start());
        System.err.println(analyzer.getOutput());

        loadJvmAndProject();
    }

    @Test
    public void loadJvmAndProject(){

        boolean debug = true;

        className = className.substring(0, className.lastIndexOf("_"));

        /**
         * 01 初始化运行环境、JVM、以及待测PROJECT
         */
        DTConfiguration.setJvmDepensRoot(jvmpath);
        DTConfiguration.setProjectDepensRoot(originProjPath);

        System.out.println(DTPlatform.getInstance());
        ArrayList<JvmInfo> jvmCmds = DTLoader.getInstance().loadJvms();

        for (JvmInfo jvmCmd : jvmCmds) {
            System.out.println(jvmCmd);
        }
        ProjectInfo mutateProject =
                DTLoader.getInstance().loadTargetProjectWithGivenPath(
                        mutateProjPath,
                        projectName,
                        null,
                        false);

        ProjectInfo mutationProvideProj =
                DTLoader.getInstance().loadTargetProjectWithGivenPath(
                        mutationProjectPath,
                        mutationProvideProjS,
                        null,
                        false);

        if (debug){

            HashMap<String, JvmOutput> results = new HashMap<>();
            for (JvmInfo cmd : jvmCmds) {

                System.out.println("= = = = = = = = = = = = = " + cmd.getJvmId() + " = = = = = = = = = = = = = = = = = = ");
                String javaCmd = cmd.getJavaCmd();

                String cmdExecutor;
                if (crossProject){
                    cmdExecutor = ExecutorHelper.assembleJavaCmd(javaCmd,
                            null,
                            mutateProject.getpClassPath() + DTPlatform.PATH_SEPARATOR + mutationProvideProj.getpClassPath(),
                            className,
                            isJunit,
                            new String[]{});
                } else {
                    cmdExecutor = ExecutorHelper.assembleJavaCmd(javaCmd,
                            null,
                            mutateProject.getpClassPath(),
                            className,
                            isJunit,
                            new String[]{});
                }

                String jvmId = cmd.getJvmId() != null ? cmd.getJvmId() : cmd.getFolderName();

                System.out.println(cmdExecutor);

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
                    System.out.println("TIMEOUT");
                    results.put(jvmId, new JvmOutput(""));
                }
            }

//            DiffCore diff = JDKAnalyzer.getInstance().analysis(className, results);
//            if (diff != null){
//
//                ExecutorHelper.logJvmOutput("Main", className, diff ,results);
//            }
//            if (JDKAnalyzer.getInstance().analysis(className, results)){
//                ExecutorHelper.logJvmOutput(projectName, className, results);
//            }

        } else {

            for (JvmInfo cmd : jvmCmds) {

                System.out.println("= = = = = = = = = = = = = " + cmd.getJvmName() + " = = = = = = = = = = = = = = = = = = ");
                String javaCmd = cmd.getJavaCmd();
//                String cmdExecutor = javaCmd + " -cp " + classPath  + " org.junit.runner.JUnitCore " + className;
                String cmdExecutor = ExecutorHelper.assembleJavaCmd(javaCmd, null, mutateProject.getpClassPath(), className, isJunit, new String[]{});
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
        String packageName = className.substring(0, className.lastIndexOf("_"));
        String classPath = mutationHistoryPath + DTPlatform.FILE_SEPARATOR +
                packageName + DTPlatform.FILE_SEPARATOR +
                className + ".class";

        String byteCodeFilePath = byteCodePath + DTPlatform.FILE_SEPARATOR +
                "mutate" + DTPlatform.FILE_SEPARATOR +
                className + ".txt";
        LinkedList<String> cmdArgs = new LinkedList<>();
        Collections.addAll(cmdArgs,
                tool,
                "-v",
                "-p",
                classPath
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
        ProjectInfo originProject = DTLoader.getInstance().loadTargetProjectWithGivenPath(originProjPath, projectName, null, false);

        String orginClassname = className.substring(0, className.indexOf("_"));

        String cpath = orginClassname.replace(".", DTPlatform.FILE_SEPARATOR) + ".class";
        String classPath = originProject.getSrcClassPath() + DTPlatform.FILE_SEPARATOR + cpath;

        String byteCodeFilePath = byteCodePath + DTPlatform.FILE_SEPARATOR +
                "origin" + DTPlatform.FILE_SEPARATOR +
                className + ".txt";
        LinkedList<String> cmdArgs = new LinkedList<>();
        Collections.addAll(cmdArgs,
                tool,
                "-v",
                "-p",
                classPath
        );

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs);
        OutputAnalyzer analyzer = new OutputAnalyzer(processBuilder.start());
        System.err.println(analyzer.getStderr());
        analyzer.outputToPath(byteCodeFilePath);
    }
}
