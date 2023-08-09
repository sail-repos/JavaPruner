package Utils;

import soot.Printer;
import soot.Scene;
import soot.SootClass;
import soot.baf.BafASMBackend;
import soot.options.Options;

import java.io.*;
import java.util.LinkedList;

public class ClassUtil {

    public static LinkedList<String> excludePathList;

    public static void initSoot(){
        String classPath = System.getProperty("java.class.path");
        Options.v().set_soot_classpath(classPath);
//        Scene.v().loadNecessaryClasses();

        Options.v().set_ignore_resolving_levels(true);
        Options.v().set_whole_program(true);
        Options.v().set_app(true);
        excludeJDKLibrary();
    }

    public static void initSootEnvWithClassPath(String classPath){
        String claspath = System.getProperty("java.class.path");
        claspath = classPath + System.getProperty("path.separator") + claspath;
        Options.v().set_soot_classpath(claspath);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_java_version(Options.java_version_8);
        Options.v().set_wrong_staticness(Options.wrong_staticness_ignore);

        Options.v().set_ignore_resolving_levels(true);
        Options.v().set_whole_program(true);
        Options.v().set_app(true);
        excludeJDKLibrary();
    }

    public static void initSootEnvWithClassPath(String classPath, int src_prec, int output_format){
        String claspath = System.getProperty("java.class.path");
        claspath = classPath + System.getProperty("path.separator") + claspath;
        Options.v().set_soot_classpath(claspath);
        Options.v().set_src_prec(src_prec);
        Options.v().set_output_format(output_format);
        Options.v().set_java_version(Options.java_version_8);
        Options.v().set_wrong_staticness(Options.wrong_staticness_ignore);

        Options.v().set_ignore_resolving_levels(true);
        Options.v().set_whole_program(true);
        Options.v().set_app(true);
        excludeJDKLibrary();
    }

    public static SootClass loadClass(String className){
        try {
            SootClass sootClass = Scene.v().forceResolve(className, SootClass.BODIES);
            Scene.v().loadNecessaryClasses();
            return sootClass;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void saveClass(SootClass sootClass, String dirPath, String fileName){
        String jimpleFilePath = dirPath + "/" + fileName  + ".jimple";
        String classFilePath = dirPath + "/" + fileName  + ".class";
        try{
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdir();
            OutputStream jimpleStreamOut = new FileOutputStream(jimpleFilePath);
            PrintWriter jimpleWriterOut = new PrintWriter(new OutputStreamWriter(jimpleStreamOut));
            //01 jimple
            Printer.v().printTo(sootClass, jimpleWriterOut);
            jimpleStreamOut.flush();
            jimpleWriterOut.flush();
            jimpleStreamOut.close();

            OutputStream classStreamOut = new FileOutputStream(classFilePath);
            //class
            BafASMBackend backend = new BafASMBackend(sootClass, Options.v().java_version());
            backend.generateClassFile(classStreamOut);
            classStreamOut.flush();
            classStreamOut.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void stageClass(SootClass sootClass, String dirPath){
        String jimpleFilePath = dirPath + "/" + sootClass.getName()  + "-stage.jimple";
        String classFilePath = dirPath + "/" + sootClass.getName()  + "-stage.class";
        try{
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdir();
            OutputStream jimpleStreamOut = new FileOutputStream(jimpleFilePath);
            PrintWriter jimpleWriterOut = new PrintWriter(new OutputStreamWriter(jimpleStreamOut));
            //01 jimple
            Printer.v().printTo(sootClass, jimpleWriterOut);
            jimpleStreamOut.flush();
            jimpleWriterOut.flush();
            jimpleStreamOut.close();

            OutputStream classStreamOut = new FileOutputStream(classFilePath);
            //class
            BafASMBackend backend = new BafASMBackend(sootClass, Options.v().java_version());
            backend.generateClassFile(classStreamOut);
            classStreamOut.flush();
            classStreamOut.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void excludeJDKLibrary() {
        //exclude jdk classes
        Options.v().set_exclude(excludeList());
//        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
    }

    private static LinkedList<String> excludeList() {

        if(excludePathList == null) {
            excludePathList = new LinkedList<String> ();
            excludePathList.add("java.");
            excludePathList.add("javax.");
            excludePathList.add("sun.");
            excludePathList.add("sunw.");
            excludePathList.add("com.sun.");
            excludePathList.add("com.ibm.");
            excludePathList.add("com.apple.");
            excludePathList.add("apple.awt.");
        }
        return excludePathList;
    }
}
