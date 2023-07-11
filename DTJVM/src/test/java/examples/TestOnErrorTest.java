package examples;

import org.junit.Test;
import java.lang.management.ManagementFactory;
import java.util.Properties;

import com.sun.management.HotSpotDiagnosticMXBean;

public class TestOnErrorTest {

    @Test
    public void test(){

//        String jdkDebug = System.getProperty("jdk.debug");
//        jdkDebug.toLowerCase().contains("debug");
//        System.out.println(jdkDebug);
        if (!Platform.isDebugBuild()) {

            System.out.println("Test requires a non-product build - skipping");
            return;
        }

    }

    @Test
    public void test2(){

        Properties properties = System.getProperties();

        properties.keySet().forEach(System.out::println);
        //java.vm.vendor
        //java.class.path
        //idea.test.cyclic.buffer.size
        //java.vm.specification.version
        //sun.java.launcher
        //sun.boot.library.path
        //sun.java.command
    }
}
