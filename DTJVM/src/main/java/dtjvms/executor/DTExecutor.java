package dtjvms.executor;

import dtjvms.*;
import dtjvms.analyzer.DiffCore;
import dtjvms.analyzer.JDKAnalyzer;
import dtjvms.executor.JIT.JITExecutor;
import dtjvms.executor.JIT.JvmOutput;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DTExecutor extends Executor{

    private JvmOutput currentOutput;
    private Process currentProcess;
    private boolean debug = false;

    public static DTExecutor dtExecutor;

    public static DTExecutor getInstance(){
        if (dtExecutor == null){
            dtExecutor = new DTExecutor();
        }
        return dtExecutor;
    }

    @Override
    public JvmOutput execute(String cmd) {

        JvmOutput output = null;
        currentOutput = null;
        try {
            currentProcess = Runtime.getRuntime().exec(cmd);
            output = ExecutorHelper.getJvmOutput(currentProcess);
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentOutput = output;
        return output;
    }

    public JvmOutput execute(String cmd, String runDir) {

        JvmOutput output = null;
        currentOutput = null;
        try {
            if (runDir != null) {
                System.out.println(cmd);
                currentProcess = Runtime.getRuntime().exec(cmd, null, new File(runDir));

            } else {
                currentProcess = Runtime.getRuntime().exec(cmd);
            }
            output = ExecutorHelper.getJvmOutput(currentProcess);
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentOutput = output;
        return output;
    }

    public void dtSingleClassInProj(ArrayList<JvmInfo> jvmCmds, ProjectInfo currentProject, String classname){

        ArrayList<String> vmOptions = currentProject.getVmoptions();
        String classPath = currentProject.getpClassPath();
        /**
         * 01 differential testing java application class
         */
        if (currentProject.getApplicationClasses().contains(classname)){

            System.out.println("Project-application: "
                    + currentProject.getProjectName()
                    + classname
                    + "...");

            Thread ctester = new Thread(new Runnable() {
                @Override
                public void run() {
                    dtSingleClass(jvmCmds, vmOptions, classPath, currentProject.getProjectName() , classname, false, new String[]{});
                }
            });
            ctester.start();
            try {
                TimeUnit.SECONDS.timedJoin(ctester, DTConfiguration.CLASS_MAX_RUNTIME);
                if (ctester.isAlive() || ctester.isInterrupted()){
                    ctester.stop();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        /**
         * 02 differential testing junit test case
         */
        if (currentProject.getJunitClasses().contains(classname)){

            System.out.println("Project-junit: "
                    + currentProject.getProjectName()
                    + classname
                    + "...");

            ThreadGroup threadGroup = new ThreadGroup("Tester");
            Thread ctester = new Thread(threadGroup, new Thread(new Runnable() {
                @Override
                public void run() {
                    dtSingleClass(jvmCmds, vmOptions, classPath, currentProject.getProjectName() ,classname, true, new String[]{});
                }
            }));
            ctester.start();
            try {
                TimeUnit.SECONDS.timedJoin(ctester, DTConfiguration.CLASS_MAX_RUNTIME);
                threadGroup.interrupt();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public HashMap<String, JvmOutput> dtSingleClass(ArrayList<JvmInfo> jvms,
                                                    ArrayList<String> pOptions,
                                                    String classpath,
                                                    String projName,
                                                    String className,
                                                    boolean isJunit,
                                                    String... args){

        HashMap<String, JvmOutput> results = new HashMap<>();

        for (JvmInfo jvm : jvms) {

            String jvmId = jvm.getJvmId() != null ? jvm.getJvmId() : jvm.getFolderName();
            String cmdExecute = ExecutorHelper.assembleJavaCmd(jvm.getJavaCmd(), pOptions, classpath, className, isJunit, args);

            Thread ctester = new Thread(new Runnable() {
                @Override
                public void run() {
                    getInstance().execute(cmdExecute, classpath);
                }
            });
            ctester.start();
            try {
                TimeUnit.SECONDS.timedJoin(ctester, DTConfiguration.CLASS_MAX_RUNTIME);
                JITExecutor.getInstance().shutDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (currentOutput != null){
                results.put(jvmId, currentOutput);
            } else {
                results.put(jvmId, new JvmOutput("JvmOutput-TIMEOUT"));
            }
            if (debug) {
                System.out.println(String.join("", Collections.nCopies(50,"=")) +
                        jvm.getJvmName() + "-" + jvm.getVersion() + String.join("", Collections.nCopies(50,"=")));
                System.out.println(currentOutput.getOutput());
            }
        }
        return results;
    }

    public void enableDebugMode() {
        debug = true;
    }

    public void disableDebugMode() {
        debug = false;
    }

}
