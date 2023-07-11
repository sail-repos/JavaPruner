package dtjvms.executor.JIT;

import dtjvms.*;
import dtjvms.analyzer.DiffCore;
import dtjvms.analyzer.JDKAnalyzer;
import dtjvms.executor.Executor;
import dtjvms.executor.ExecutorHelper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class JITExecutor extends Executor {

    private JvmOutput currentOutput;
    private Process currentProcess;

    private boolean debug = true;

    private boolean diffFound;
    private boolean disCard;

    public static JITExecutor cfmExecutor;

    private HashMap<String, JvmOutput> lastResults = new HashMap<>();

    public static JITExecutor getInstance(){

        if (cfmExecutor == null){
            cfmExecutor = new JITExecutor();
        }
        return cfmExecutor;
    }

    private long getCurrentProcessId() throws Exception {

        long pid = 0;
        if (DTPlatform.isLinux() || DTPlatform.isMac()) {
            Field f = currentProcess.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            pid = f.getLong(currentProcess);
            f.setAccessible(false);
        } else if (DTPlatform.isWin()) {
            //TODO
        } else {
            throw new RuntimeException("UNKNOWN OS");
        }
        return pid;
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

    public void shutDown(){

        if (currentProcess != null){
            currentProcess.destroy();
            currentProcess.destroyForcibly();
        }
    }

    public boolean isDiffFound() {
        return diffFound;
    }

    public boolean isDisCard() {
        return disCard;
    }

    public void dtSingleClassMultiThread(ArrayList<JvmInfo> jvms,
                                         ArrayList<String> pOptions,
                                         String classpath,
                                         String projName,
                                         String className,
                                         boolean isJunit,
                                         String... args) {

        HashMap<String, JvmOutput> results = new HashMap<>();

        for (JvmInfo jvm : jvms) {

            String jvmId = jvm.getJvmId() != null ? jvm.getJvmId() : jvm.getFolderName();
            String cmdExecute = ExecutorHelper.assembleJavaCmd(jvm.getJavaCmd(), pOptions, classpath, className, isJunit, args);

            Thread ctester = new Thread(new Runnable() {
                @Override
                public void run() {
                    getInstance().execute(cmdExecute);
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
        }

        if (JDKAnalyzer.getInstance().analysis(className, results) != null){
            ExecutorHelper.logJvmOutput(projName, className, results);
        }
    }


    public HashMap<String, JvmOutput> dtSingleClass(ArrayList<JvmInfo> jvms,
                                                    ArrayList<String> pOptions,
                                                    String classpath,
                                                    String projName,
                                                    String className,
                                                    boolean isJunit,
                                                    String... args) {

        HashMap<String, JvmOutput> results = new HashMap<>();

        for (JvmInfo jvm : jvms) {

            String options = "";
            if(jvm.getVmOptions() != null){

                options = " -Xcomp -XX:+UnlockDiagnosticVMOptions -XX:+UnlockExperimentalVMOptions -XX:+IgnoreUnrecognizedVMOptions -XX:CompileThreshold=100";
                Set<Integer> indexSet = new HashSet<>();
                for(int i = 0; i < new Random().nextInt(jvm.getVmOptions().getOptions().size()); i++){
                    indexSet.add(new Random().nextInt(jvm.getVmOptions().getOptions().size()));
                }
                for (Integer integer : indexSet) {
                    options = options + " "  + jvm.getVmOptions().getOptions().get(integer).getCmd();
                }
            }

            String jvmId = jvm.getJvmId() != null ? jvm.getJvmId() : jvm.getFolderName();
            String cmdExecute = ExecutorHelper.assembleJavaCmd(jvm.getJavaCmd() + options, pOptions, classpath, className, isJunit, args);
//            System.out.println("cmdExecute: " + cmdExecute);
            Thread ctester = new Thread(new Runnable() {
                @Override
                public void run() {
                    getInstance().execute(cmdExecute);
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
                if(currentOutput.getStdout().contains("CHECKSUM")){
                    currentOutput.setStdout(currentOutput.getStdout().substring(currentOutput.getStdout().indexOf("CHECKSUM"))+"\n");
                    currentOutput.setStdout(currentOutput.getStdout().substring(0,currentOutput.getStdout().indexOf("\n")).replace("CHECKSUM:",""));
                }
                else {
                    currentOutput.setStdout("");
                }

                results.put(jvmId + options, currentOutput);
            } else {
                currentOutput = new JvmOutput("JvmOutput-TIMEOUT");
                results.put(jvmId + options, currentOutput);
            }
            if (debug) {
                System.out.println(String.join("", Collections.nCopies(50,"=")) +
                        jvm.getJvmName() + "-" + jvm.getVersion() + String.join("", Collections.nCopies(50,"=")));
                System.out.println(currentOutput.getOutput());
            }
        }
        return results;
    }

    public JvmOutput getCurrentOutput() {
        return currentOutput;
    }

    public void enableDebugMode() {
        debug = true;
    }

    public void disableDebugMode() {
        debug = false;
    }

    public HashMap<String, JvmOutput> getLastResults() {
        return lastResults;
    }
}
