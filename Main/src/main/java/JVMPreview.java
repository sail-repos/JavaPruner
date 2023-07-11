import dtjvms.*;
import dtjvms.executor.DTExecutor;
import dtjvms.loader.DTLoader;

import java.util.ArrayList;

public class JVMPreview {

    /**
     * Overview of the test platform, test jvms and test projects
     *
     * usage:
     *       ./scripts/DTJVM.sh
     *       ./scripts/DTJVM.sh DTPreview
     * @param args
     */
    public static void main(String[] args) {

//        DTConfiguration.setJvmDepensRoot("." + DTPlatform.FILE_SEPARATOR + "01JVMS");
        DTConfiguration.setJvmDepensRoot(".\\JVMs");
//        DTConfiguration.setProjectDepensRoot("." + DTPlatform.FILE_SEPARATOR + "02Benchmarks");

        /**
         * Testing platform
         */
        System.out.println(DTPlatform.getInstance());

        ArrayList<JvmInfo> jvmCmds = DTLoader.getInstance().loadJvms();
//        ArrayList<ProjectInfo> projectInfos = DTLoader.loadTestProjectsWithBootJvms(jvmCmds);

        /**
         * JVM
         */
        for (JvmInfo jvmCmd : jvmCmds) {
//            System.out.println(jvmCmd);

//            System.out.println("= = = = = = = = = = = = = " + jvmCmd.getJvmName() + " = = = = = = = = = = = = = = = = = = ");
            String cmdExecutor = jvmCmd.getJavaCmd() + " -version";
            System.out.println(cmdExecutor);
            DTExecutor.getInstance().execute(cmdExecutor);
        }

        /**
         * Projects
         */
//        for (ProjectInfo projectInfo : projectInfos) {
//
//            System.out.println(projectInfo);
////            if (projectInfo.getApplicationClasses().size() > 0){
////                projectInfo.getApplicationClasses().forEach(System.out::println);
////            }
//        }
    }
}
