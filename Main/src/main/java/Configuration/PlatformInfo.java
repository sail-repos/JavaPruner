package Configuration;

public class PlatformInfo {

    public static String OS_NAME = System.getProperty("os.name").toLowerCase();

    public static boolean isMac(){
        return OS_NAME.indexOf("mac") >= 0;
    }

    public static boolean isLinux(){
        return OS_NAME.indexOf("linux") >= 0;
    }

    public static boolean isWin(){
        return OS_NAME.indexOf("windows") >= 0;
    }

}
