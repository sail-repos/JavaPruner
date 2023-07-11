import dtjvms.analyzer.JDKAnalyzer;
import dtjvms.executor.JIT.JITExecutor;
import dtjvms.executor.JIT.JvmOutput;
import dtjvms.executor.ExecutorHelper;

import java.util.HashMap;

public class ProcessTest {

    //Junit的异常从 stdout 中输出
    public static void main(String[] args) {

        String openjdk8_Hotspot_cmd = "/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/01JVMS/macOSx64/openjdk8/OpenJDK8U-jre_x64_mac_hotspot_8u275b01/Contents/Home/bin/java  -cp /Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/junit-4.10.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/byte-buddy-0.6.8.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/fest-assert-1.3.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/powermock-reflect-1.2.5.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/asm-3.1.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/objenesis-tck-3.1.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/out/production/Mockito-BugRepro-1:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/out/test/Mockito-BugRepro-1 org.junit.runner.JUnitCore org.mockito.internal.invocation.InvocationsFinderTest";
        String openjdk8_OpenJ9_cmd = "/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/01JVMS/macOSx64/openjdk8/OpenJDK8U-jre_x64_mac_openj9_8u292b10_openj9-0.26.0/Contents/Home/bin/java  -cp /Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/junit-4.10.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/byte-buddy-0.6.8.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/fest-assert-1.3.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/powermock-reflect-1.2.5.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/asm-3.1.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/objenesis-tck-3.1.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/out/production/Mockito-BugRepro-1:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/out/test/Mockito-BugRepro-1 org.junit.runner.JUnitCore org.mockito.internal.invocation.InvocationsFinderTest ";
        String openjdk9_Hotspot_cmd = "/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/01JVMS/macOSx64/openjdk9/OpenJDK9U-jre_x64_mac_hotspot_9_181/Contents/Home/bin/java  -cp /Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/junit-4.10.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/byte-buddy-0.6.8.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/fest-assert-1.3.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/powermock-reflect-1.2.5.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/asm-3.1.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/objenesis-tck-3.1.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/out/production/Mockito-BugRepro-1:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/out/test/Mockito-BugRepro-1 org.junit.runner.JUnitCore org.mockito.internal.invocation.InvocationsFinderTest";
        String openjdk11_Graalvm_cmd = "/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/01JVMS/macOSx64/openjdk11/graalvm-ce-java11-21.1.0/Contents/Home/bin/java  -cp /Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/junit-4.10.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/byte-buddy-0.6.8.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/fest-assert-1.3.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/powermock-reflect-1.2.5.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/asm-3.1.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/objenesis-tck-3.1.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/out/production/Mockito-BugRepro-1:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/out/test/Mockito-BugRepro-1 org.junit.runner.JUnitCore org.mockito.internal.invocation.InvocationsFinderTest";
        String openjdk11_OpenJ9_cmd = "/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/01JVMS/macOSx64/openjdk11/OpenJDK11U-jre_x64_mac_openj9_11.0.11_9_openj9-0.26.0/Contents/Home/bin/java  -cp /Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/junit-4.10.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/byte-buddy-0.6.8.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/fest-assert-1.3.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/powermock-reflect-1.2.5.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/asm-3.1.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/objenesis-tck-3.1.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/out/production/Mockito-BugRepro-1:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/out/test/Mockito-BugRepro-1 org.junit.runner.JUnitCore org.mockito.internal.invocation.InvocationsFinderTest";
        String openjdk11_Hotspot_cmd = "/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/01JVMS/macOSx64/openjdk11/OpenJDK11U-jre_x64_mac_hotspot_11.0.10_9/Contents/Home/bin/java  -cp /Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/junit-4.10.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/byte-buddy-0.6.8.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/fest-assert-1.3.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/powermock-reflect-1.2.5.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/asm-3.1.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/objenesis-tck-3.1.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/out/production/Mockito-BugRepro-1:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/out/test/Mockito-BugRepro-1 org.junit.runner.JUnitCore org.mockito.internal.invocation.InvocationsFinderTest";
        String openjdk8_OpenJ9_Xfuture_cmd = "/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/01JVMS/macOSx64/openjdk8/OpenJDK8U-jre_x64_mac_openj9_8u292b10_openj9-0.26.0/Contents/Home/bin/java -Xfuture  -cp /Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/junit-4.10.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/byte-buddy-0.6.8.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/fest-assert-1.3.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/powermock-reflect-1.2.5.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/asm-3.1.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/lib/objenesis-tck-3.1.jar:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/out/production/Mockito-BugRepro-1:/Users/yingquanzhao/Documents/LearnFile/JVM/01Project/CFMutation/sootOutput/Mockito-BugRepro-1/out/test/Mockito-BugRepro-1 org.junit.runner.JUnitCore org.mockito.internal.invocation.InvocationsFinderTest";


        JITExecutor.getInstance().execute(openjdk8_Hotspot_cmd);
        JvmOutput openjdk8_Hotspot_output = JITExecutor.getInstance().getCurrentOutput();
        System.out.println("==================openjdk8_Hotspot_output===================");
        System.out.println(openjdk8_Hotspot_output.getOutput());

        JITExecutor.getInstance().execute(openjdk8_OpenJ9_cmd);
        JvmOutput openjdk8_OpenJ9_output = JITExecutor.getInstance().getCurrentOutput();
        System.out.println("==================openjdk8_OpenJ9_output===================");
        System.out.println(openjdk8_OpenJ9_output.getOutput());

        JITExecutor.getInstance().execute(openjdk9_Hotspot_cmd);
        JvmOutput openjdk9_Hotspot_output = JITExecutor.getInstance().getCurrentOutput();
        System.out.println("==================openjdk9_Hotspot_output===================");
        System.out.println(openjdk9_Hotspot_output.getOutput());

        JITExecutor.getInstance().execute(openjdk11_Graalvm_cmd);
        JvmOutput openjdk11_Graalvm_output = JITExecutor.getInstance().getCurrentOutput();
        System.out.println("==================openjdk11_Graalvm_output===================");
        System.out.println(openjdk11_Graalvm_output.getOutput());

        JITExecutor.getInstance().execute(openjdk11_OpenJ9_cmd);
        JvmOutput openjdk11_OpenJ9_output = JITExecutor.getInstance().getCurrentOutput();
        System.out.println("==================openjdk11_OpenJ9_output===================");
        System.out.println(openjdk11_OpenJ9_output.getOutput());

        JITExecutor.getInstance().execute(openjdk11_Hotspot_cmd);
        JvmOutput openjdk11_Hotspot_output = JITExecutor.getInstance().getCurrentOutput();
        System.out.println("==================openjdk11_Hotspot_output===================");
        System.out.println(openjdk11_Hotspot_output.getOutput());

        JITExecutor.getInstance().execute(openjdk8_OpenJ9_Xfuture_cmd);
        JvmOutput openjdk8_OpenJ9_Xfuture_output = JITExecutor.getInstance().getCurrentOutput();
        System.out.println("==================openjdk8_OpenJ9_Xfuture_output===================");
        System.out.println(openjdk8_OpenJ9_Xfuture_output.getOutput());

        HashMap<String, JvmOutput> results = new HashMap<>();

        results.put("openjdk8-Hotspot", openjdk8_Hotspot_output);
        results.put("openjdk8-OpenJ9", openjdk8_OpenJ9_output);
        results.put("openjdk9-Hotspot", openjdk9_Hotspot_output);
        results.put("openjdk11_Graalvm", openjdk11_Graalvm_output);
        results.put("openjdk11_OpenJ9", openjdk11_OpenJ9_output);
        results.put("openjdk11_Hotspot", openjdk11_Hotspot_output);
        results.put("penjdk8_OpenJ9_Xfuture", openjdk8_OpenJ9_Xfuture_output);

//        JDKAnalyzer.getInstance().analysis("org.mockito.internal.invocation.InvocationsFinderTest", results);
//        System.out.println(JDKAnalyzer.getInstance().analysis("org.mockito.internal.invocation.InvocationsFinderTest", results));

//        if (JDKAnalyzer.getInstance().analysis("org.mockito.internal.invocation.InvocationsFinderTest", results)){
//            ExecutorHelper.logJvmOutput("Test", "logClassName", results);
//        }

//        System.out.println("=============getStderr==============");
//        System.out.println(output1.getStderr());
//        System.out.println("=============getStdout==============");
//        System.out.println(output1.getStdout());
//        System.out.println("=============getStdout==============");
//        System.out.println(output1.getOutput());
    }
}
