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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class DTClass {

    public static String jvmpath = "/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/01JVMS";
    public static String projectName = "Openj9Test-Test";
    public static String projPath = "/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/ClassValidate/Benchmarks/Origin";

    public static boolean debug = true;
    public static boolean isJunit = false;

    public static String className = "javaT.nio.Buffer.CharAt";

    public static void main(String[] args) {

        DTConfiguration.setJvmDepensRoot("/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/DTJVM/JVM-depens");
        DTConfiguration.setProjectDepensRoot("/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/ClassValidate/Benchmarks/Origin");

        /**
         * Testing platform
         */
        System.out.println(DTPlatform.getInstance());

        ArrayList<JvmInfo> jvmCmds = DTLoader.getInstance().loadJvms();
        ProjectInfo projectInfo = DTLoader.getInstance().loadTargetProjectWithGivenPath(projPath, projectName, jvmCmds, false);

        /**
         * JVM
         */
        for (JvmInfo jvmCmd : jvmCmds) {
            System.out.println(jvmCmd);
        }

        if (debug){

            HashMap<String, JvmOutput> results = new HashMap<>();
            for (JvmInfo cmd : jvmCmds) {

                System.out.println("= = = = = = = = = = = = = " + cmd.getJvmId() + " = = = = = = = = = = = = = = = = = = ");
                String javaCmd = cmd.getJavaCmd();
                String cmdExecutor = ExecutorHelper.assembleJavaCmd(javaCmd,null, projectInfo.getpClassPath(), className, isJunit ,new String[]{});
                String jvmId = cmd.getJvmId() != null ? cmd.getJvmId() : cmd.getFolderName();

//                System.out.println(cmdExecutor);

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
                String cmdExecutor = ExecutorHelper.assembleJavaCmd(javaCmd, null, projectInfo.getpClassPath(), className, isJunit, new String[]{});
                System.out.println(cmdExecutor);
                DTExecutor.getInstance().execute(cmdExecutor);
            }
        }
    }
}
