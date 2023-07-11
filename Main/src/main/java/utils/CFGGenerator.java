package utils;

import dtjvms.ProjectInfo;
import dtjvms.loader.DTLoader;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.util.cfgcmd.CFGGraphType;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

import java.io.File;
import java.io.FileFilter;

public class CFGGenerator {

    public static String outputDir = "../CFG";

    public static CFGGraphType graphtype;
    public static CFGToDotGraph drawer = new CFGToDotGraph();

    public static void main(String[] args) {

        ProjectInfo project = DTLoader.getInstance().loadTargetProjectWithGivenPath("sootOutput", "Templates", null, false);

        String mainclass = "demo7";
        String defaultGraph = "BriefUnitGraph";
        graphtype = CFGGraphType.getGraphType(defaultGraph);

        ClassUtils.initSootEnvWithClassPath(project.getpClassPath());

//        String claspath = System.getProperty("java.class.path");
//        Scene.v().setSootClassPath(claspath);
//        Scene.v().loadNecessaryClasses();

        SootClass sootClass = ClassUtils.loadClass(mainclass);
        PackManager.v().runPacks();
        CallGraph cg = Scene.v().getCallGraph();

        for (SootMethod method : sootClass.getMethods()) {
            if (!method.isConstructor()){

                System.out.println("Convert " + method.getName() + " to dot file...");
                Body methodBody = method.retrieveActiveBody();
                print_cfg(methodBody);
            }
        }
        //covert dot file to png
        File sootOutput = new File(outputDir);

        File[] dotFiles = sootOutput.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile() && pathname.getName().endsWith(".dot")){ return true; }
                return false;
            }
        });

        for (File dotFile : dotFiles) {
            GraphViz.convertDotFileToPng(dotFile);
        }
    }

    public static void printCFG(String className, ProjectInfo project){

        ClassUtils.initSootEnvWithClassPath(project.getpClassPath());
        SootClass sootClass = ClassUtils.loadClass(className);

        String defaultGraph = "ZonedBlockGraph";
        graphtype = CFGGraphType.getGraphType(defaultGraph);

        for (SootMethod method : sootClass.getMethods()) {
            if (!method.isConstructor()){

                System.out.println("Convert " + method.getName() + " to dot file...");
                Body methodBody = method.retrieveActiveBody();
                print_cfg(methodBody);
            }
        }
        //covert dot file to png
        File sootOutput = new File(outputDir);

        File[] dotFiles = sootOutput.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile() && pathname.getName().endsWith(".dot")){ return true; }
                return false;
            }
        });

        for (File dotFile : dotFiles) {
            GraphViz.convertDotFileToPng(dotFile);
        }
    }

    public static void print_cfg(Body body) {

        DirectedGraph<Unit> graph = graphtype.buildGraph(body);
        DotGraph canvas = graphtype.drawGraph(drawer, graph, body);

        String methodname = body.getMethod().getSubSignature().replace(" ","");
        String classname = body.getMethod().getDeclaringClass().getName().replaceAll("\\$", "\\.");
        String filename = outputDir;
        if (filename.length() > 0) {
            filename = filename + File.separator;
        }

        filename = filename + classname + methodname.replace(File.separatorChar, '.') + DotGraph.DOT_EXTENSION;
        canvas.plot(filename);
    }
}