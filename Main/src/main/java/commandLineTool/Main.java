package commandLineTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) throws IOException {

        LinkedList<String> jvmOptions = new LinkedList<>();
        LinkedList<String> sootOptions = new LinkedList<>();
        ArrayList<String> cmdargs = new ArrayList<>();

        /**
         * 01 add jvm cmd and options
         */
        String classpath = System.getProperty("java.class.path");
        Collections.addAll(jvmOptions,
                "java",
                "-cp",
                classpath
        );

        //create cfg
        Collections.addAll(sootOptions,
              "soot.tools.CFGViewer",
                        "--soot-class-path",
                        classpath + ":./CommandlineTest",
                        "--graph=BriefBlockGraph",
                        "JimpleMixer.benchmarks.codeProvider");

        cmdargs.addAll(jvmOptions);
        cmdargs.addAll(sootOptions);

        System.out.println(cmdargs);
        ProcessBuilder processBuilder = new ProcessBuilder(cmdargs.toArray(new String[cmdargs.size()]));
        OutputAnalyzer analyzer = new OutputAnalyzer(processBuilder.start());
        System.out.println(analyzer.getOutput());
    }
}
