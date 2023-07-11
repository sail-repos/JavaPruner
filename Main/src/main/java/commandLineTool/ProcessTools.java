package commandLineTool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ProcessTools {

    /**
     * create process with the given cmd
     * @param cmd
     * @return
     */
    public static ProcessBuilder createProcessCmd(String cmd){

        String[] cmdArgs = cmd.split(" ");
        return new ProcessBuilder(cmdArgs);
    }

    /**
     * Create ProcessBuilder using the java launcher from the jdk to be tested,
     * and with any platform specific arguments prepended.
     *
     * @param command Arguments to pass to the java command.
     * @return The ProcessBuilder instance representing the java command.
     */
    public static ProcessBuilder createJavaProcessBuilder(String... command) {

        String javapath = System.getProperty("java.home");
        javapath = javapath + "/bin/java";

        ArrayList<String> args = new ArrayList<>();
        args.add(javapath);

        args.add("-cp");
        args.add(System.getProperty("java.class.path"));

        Collections.addAll(args, command);

        // Reporting
        StringBuilder cmdLine = new StringBuilder();
        for (String cmd : args)
            cmdLine.append(cmd).append(' ');

        System.out.println("Command line: [" + cmdLine.toString() + "]");

        return new ProcessBuilder(args.toArray(new String[args.size()]));
    }

    /**
     * Pumps stdout and stderr the running process into a String.
     *
     * @param process Process to pump.
     * @return Output from process.
     * @throws IOException If an I/O error occurs.
     */
    public static OutputBuffer getOutput(Process process) throws IOException {

        ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
        StreamPumper outPumper = new StreamPumper(process.getInputStream(), stdoutBuffer);
        StreamPumper errPumper = new StreamPumper(process.getErrorStream(), stderrBuffer);
        Thread outPumperThread = new Thread(outPumper);
        Thread errPumperThread = new Thread(errPumper);

        outPumperThread.setDaemon(true);
        errPumperThread.setDaemon(true);

        outPumperThread.start();
        errPumperThread.start();

        try {
            process.waitFor();
            outPumperThread.join();
            errPumperThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }

        return new OutputBuffer(stdoutBuffer.toString(), stderrBuffer.toString());
    }

    public static void main(String[] args) {

        String javapath = System.getProperty("java.home");
        javapath = javapath + "/bin/java";
        System.out.println(javapath);
    }
}
