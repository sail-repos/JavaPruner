package commandLineTool;

import java.io.IOException;

public class Terminal {
    public static void main(String[] args) throws IOException {

//        ProcessBuilder processBuilder = ProcessTools.createJavaProcessBuilder("-version");
        ProcessBuilder processBuilder = ProcessTools.createProcessCmd("ls -al");
        OutputAnalyzer analyzer = new OutputAnalyzer(processBuilder.start());
        System.out.println(analyzer.getOutput());
    }
}
