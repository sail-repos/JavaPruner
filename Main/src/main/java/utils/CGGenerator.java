package utils;

import dtjvms.ProjectInfo;
import dtjvms.loader.DTLoader;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.dot.DotGraph;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static soot.util.dot.DotGraph.DOT_EXTENSION;

public class CGGenerator {

    public static String outputDir = "../CG";
    public static boolean allMethods = false;

    public static void main(String[] args) {

        ProjectInfo project = DTLoader.getInstance().loadTargetProjectWithGivenPath("sootOutput", "Templates", null, false);

        String mainclass = "demo11";
        ClassUtils.initSootEnvWithClassPath(project.getpClassPath());

        SootClass sootClass = ClassUtils.loadClass(mainclass);
        PackManager.v().runPacks();

        print_cg(sootClass, Scene.v().getCallGraph());

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

    public static void printCG(String className, ProjectInfo project) {

        ClassUtils.initSootEnvWithClassPath(project.getpClassPath());
        SootClass sootClass = ClassUtils.loadClass(className);
        PackManager.v().runPacks();
        print_cg(sootClass, Scene.v().getCallGraph());

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

    public static void print_cg(SootClass sootClass, CallGraph callGraph) {

        Set<String> visitedNodes = new HashSet<>();
        Set<String> visitedEdges = new HashSet<>();

        String classname = sootClass.getName();
        String filename = outputDir + File.separator + classname + DOT_EXTENSION;

        DotGraph canvas = new DotGraph("call-graph");

        for (SootMethod method : sootClass.getMethods()) {

            if (method.hasActiveBody() == false) {
                continue;
            }
            Iterator<Edge> edges = callGraph.edgesOutOf(method);
            while (edges.hasNext()) {
                Edge edge = edges.next();
                SootMethod pointToMethod = (SootMethod) edge.getTgt();
                if (allMethods || sootClass.getMethods().contains(pointToMethod)) {

                    if (pointToMethod.isConstructor() || pointToMethod.isStaticInitializer()) {
                        continue;
                    }
                    if (!visitedNodes.contains(method.toString())){
                        canvas.drawNode(method.toString());
                        visitedNodes.add(method.toString());
                    }
                    if (!visitedNodes.contains(pointToMethod.toString())){
                        canvas.drawNode(pointToMethod.toString());
                        visitedNodes.add(pointToMethod.toString());
                    }
                    if (!visitedEdges.contains(method + "->" + pointToMethod)){
                        canvas.drawEdge(method.toString(), pointToMethod.toString());
                        visitedEdges.add(method + "->" + pointToMethod);
                        System.out.println(method.getSignature() + " calls --->  "+ pointToMethod.getSignature());
                    }
                }
            }
        }
        canvas.plot(filename);
    }

}