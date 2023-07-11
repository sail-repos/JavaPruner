package examples;

public class Platform {

    private static final String jdkDebug    = System.getProperty("jdk.debug");

    public static boolean isDebugBuild() {
        return (jdkDebug.toLowerCase().contains("debug"));
    }
}
