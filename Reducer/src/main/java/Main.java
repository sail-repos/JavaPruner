import JimpleMixer.blocks.BlockInfo;
import JimpleMixer.blocks.MyBlockContainer;
import Utils.ClassUtil;
import soot.*;


import java.util.*;
public class Main {

    public static void main(String[] args){
        String mainclass = "com.sun.tracing.BasicFunctionality";
        ClassUtil.initSootEnvWithClassPath("D:\\master\\JVM-Testing\\Reduce\\dataset\\Projects\\Project4");
        SootClass sootClass = ClassUtil.loadClass(mainclass);

        ClassUtil.saveClass(sootClass, "sootOutput", sootClass.getShortName());
    }

}
