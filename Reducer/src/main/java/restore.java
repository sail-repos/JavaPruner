import Reducer.*;
import Utils.ClassUtil;
import dtjvms.DTConfiguration;
import dtjvms.DTPlatform;
import dtjvms.JvmInfo;
import dtjvms.executor.DTExecutor;
import dtjvms.executor.JIT.JvmOutput;
import dtjvms.loader.DTLoader;
import soot.*;
import soot.jimple.*;
import soot.options.Options;

import java.util.*;

public class restore {

    public static void main(String[] args) {

        ClassUtil.initSootEnvWithClassPath("");
        SootClass sootClass = ClassUtil.loadClass("Test");
        ClassUtil.saveClass(sootClass, "sootOutput", sootClass.getShortName());
    }

}
