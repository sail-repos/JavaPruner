package Reducer;

import Provider.PrimitiveValueProvider;
import Utils.ClassUtil;
import dtjvms.executor.JIT.JvmOutput;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.*;

public class MethodReducer extends Reducer{
    private final Map<String, List<SootMethod>> parentMap;
    private final Map<String, List<SootMethod>> childMap;

    private final Map<Unit, Set<Unit>> targetUnitMap;
    private final Map<SootMethod, List<MethodInvokeInfo>> invokeMap;

    private Set<String> reducedSet;

    private SootClass sootClass;

    private int testtype;

    public MethodReducer(){
        super();
        this.parentMap = new HashMap<>();
        this.childMap = new HashMap<>();
        this.targetUnitMap = new HashMap<>();
        this.invokeMap = new LinkedHashMap<>();
    }

    public MethodReducer(Checker checker, String classPath){
        super(checker, classPath);
        this.parentMap = new HashMap<>();
        this.childMap = new HashMap<>();
        this.targetUnitMap = new HashMap<>();
        this.invokeMap = new LinkedHashMap<>();
    }

    /**
     * 从入口方法开始遍历调用图(BFS)，尝试删除冗余方法
     * @param sootClass class
     * @param originOutput 原output信息
     */
    @Override
    public boolean doReduce(SootClass sootClass, HashMap<String, JvmOutput> originOutput, ArrayList<String> pOtions, int testtype){
        this.sootClass = sootClass;
        this.testtype = testtype;
        reducedSet = new HashSet<>();
        parentMap.clear();
        childMap.clear();
        invokeMap.clear();
        targetUnitMap.clear();

        Scene.v().setMainClass(sootClass);
        PackManager.v().runPacks();

        CallGraph callGraph = Scene.v().getCallGraph();

        // 预处理
        pretreatment();

        // 获取接口override函数
        List<String> implementationMethods = new ArrayList<>();
        for(SootClass i : sootClass.getInterfaces()){
            for(SootMethod m : i.getMethods()){
                implementationMethods.add(m.getSignature());
            }
        }

        List<SootMethod> visitedNodes = new ArrayList<>();
        List<SootMethod> sootClasses = sootClass.getMethods();
        boolean isMain = false;
        for(SootMethod sootMethod : sootClasses){
            if(sootMethod.getName().equals("main")){
                isMain = true;
            }
        }
        if(!isMain){
            return true;
        }
        SootMethod entryMethod = sootClass.getMethodByName("main");


        // 以下代码将从main函数开始逐级遍历各个节点

        Queue<SootMethod> queue = new LinkedList<>();
        queue.offer(entryMethod);
        visitedNodes.add(entryMethod);
        while(!queue.isEmpty()){
            SootMethod cur = queue.poll();
            Iterator<Edge> edges = callGraph.edgesOutOf(cur);
            while(edges.hasNext()){
                Edge edge = edges.next();
                SootMethod tgt = edge.tgt();
                if(!sootClasses.contains(tgt) || /*tgt.getSignature().equals("<" + sootClass.getShortName() + ": void <clinit>()>") || */implementationMethods.contains(tgt.getSignature()) || visitedNodes.contains(tgt)){
                    continue;
                }
                List<SootMethod> parents = parentMap.getOrDefault(tgt.getSignature(), new ArrayList<>());  // 记录父级信息
                parents.add(cur);
                parentMap.put(tgt.getSignature(), parents);

                List<SootMethod> children = childMap.getOrDefault(cur.getSignature(), new ArrayList<>());  // 记录子级信息
                children.add(tgt);
                childMap.put(cur.getSignature(), children);
                if(!visitedNodes.contains(tgt)) {
                    queue.offer(tgt);
                    visitedNodes.add(tgt);
                }
            }
        }

        Set<SootMethod> handledMethods = new HashSet<>();
        for(SootMethod method : visitedNodes){
            if(!reducedSet.contains(method.getSignature()) && !method.getName().equals("main") && !method.isConstructor() && !handledMethods.contains(method)) {
                if(method.getSignature().contains("test_2vi_off")){
                    int a = 1;
                }
                reduceMethod(method, originOutput, pOtions);
                handledMethods.add(method);
            }
        }

        this.sootClass.getMethods().removeIf(m -> !visitedNodes.contains(m) && !implementationMethods.contains(m.getSignature()) && !m.isConstructor()/*&& !m.getSignature().equals("void <clinit>()")*/);
//        ClassUtil.saveClass(this.sootClass, "sootOutput", this.sootClass.getShortName());
        ClassUtil.saveClass(sootClass, checker.getDir(), this.sootClass.getShortName());

        return checker.bugChecker(originOutput, sootClass.getName(), pOtions, new String[1], testtype);
    }

    /**
     * 预处理
     */
     private void pretreatment(){
        for(SootMethod sootMethod : sootClass.getMethods()){
            Body body = sootMethod.retrieveActiveBody();
            UnitPatchingChain units = body.getUnits();
            Unit unit = units.getLast();
            while(unit != null){
//                if(unit instanceof IfStmt){
//                    Unit tar = ((IfStmt) unit).getTarget();
//                    Set<Unit> set = targetUnitMap.getOrDefault(tar, new HashSet<>());
//                    set.add(unit);
//                    targetUnitMap.put(tar, set);
//                }else if(unit instanceof GotoStmt){
//                    Unit tar = ((GotoStmt) unit).getTarget();
//                    Set<Unit> set = targetUnitMap.getOrDefault(tar, new HashSet<>());
//                    set.add(unit);
//                    targetUnitMap.put(tar, set);
//                }else
                if(unit instanceof JInvokeStmt){
                    SootMethod method = ((JInvokeStmt) unit).getInvokeExpr().getMethod();
                    if(sootClass.getMethods().contains(method)) {
                        List<MethodInvokeInfo> list = invokeMap.getOrDefault(method, new ArrayList<>());
                        MethodInvokeInfo methodInvokeInfo = new MethodInvokeInfo(unit, sootMethod);
                        list.add(methodInvokeInfo);
                        invokeMap.put(method, list);
                    }
                }else if(unit instanceof JAssignStmt){
                    Value rightValue = ((JAssignStmt) unit).rightBox.getValue();
                    if(rightValue instanceof JStaticInvokeExpr){
                        SootMethod method = ((JStaticInvokeExpr) rightValue).getMethod();
                        if(sootClass.getMethods().contains(method)) {
                            List<MethodInvokeInfo> list = invokeMap.getOrDefault(method, new ArrayList<>());
                            MethodInvokeInfo methodInvokeInfo = new MethodInvokeInfo(unit, sootMethod);
                            list.add(methodInvokeInfo);
                            invokeMap.put(method, list);
                        }
                    }else if(rightValue instanceof JVirtualInvokeExpr){
                        SootMethod method = ((JVirtualInvokeExpr) rightValue).getMethod();
                        if(sootClass.getMethods().contains(method)) {
                            List<MethodInvokeInfo> list = invokeMap.getOrDefault(method, new ArrayList<>());
                            MethodInvokeInfo methodInvokeInfo = new MethodInvokeInfo(unit, sootMethod);
                            list.add(methodInvokeInfo);
                            invokeMap.put(method, list);
                        }
                    }
                }
                unit = units.getPredOf(unit);
            }
        }
     }

     private void reduceMethod(SootMethod sootMethod, HashMap<String, JvmOutput> originOutput, ArrayList<String> pOtions){
         Type retType = sootMethod.getReturnType();
         if(!(retType instanceof VoidType) && PrimitiveValueProvider.next(retType) == null){
             return;
         }

         Map<Unit, Unit> invokeReplaceMap = new HashMap<>();
         Map<Unit, Unit> invokePreMap = new HashMap<>();
         Map<Unit, Value> originValueMap = new HashMap<>();

         // 将调用该方法的stmt全部删除
         List<MethodInvokeInfo> invokeList = invokeMap.get(sootMethod);
         if(invokeList == null){
             return;
         }
         targetUnitMap.clear();
         for(MethodInvokeInfo methodInvokeInfo : invokeList){
             if(reducedSet.contains(methodInvokeInfo.parentMethod.getSignature())){
                 continue;
             }

            SootMethod parentMethod = methodInvokeInfo.parentMethod;
            UnitPatchingChain pUnits = parentMethod.retrieveActiveBody().getUnits();
            for(Unit u : pUnits){
                if(u instanceof IfStmt){
                    Unit tar = ((IfStmt) u).getTarget();
                    Set<Unit> set = targetUnitMap.getOrDefault(tar, new HashSet<>());
                    set.add(u);
                    targetUnitMap.put(tar, set);
                }else if(u instanceof GotoStmt){
                    Unit tar = ((GotoStmt) u).getTarget();
                    Set<Unit> set = targetUnitMap.getOrDefault(tar, new HashSet<>());
                    set.add(u);
                    targetUnitMap.put(tar, set);
                }
            }
            UnitPatchingChain units = parentMethod.retrieveActiveBody().getUnits();
            Unit invokeUnit = methodInvokeInfo.invokeUnit;
             if(invokeUnit instanceof JAssignStmt){
                 Value originValue = ((JAssignStmt) invokeUnit).rightBox.getValue();
                 originValueMap.put(invokeUnit, originValue);
                 Value newValue = PrimitiveValueProvider.next(retType);
                 ((JAssignStmt) invokeUnit).rightBox.setValue(newValue);
             }else {
                 Unit invokePreUnit = units.getPredOf(invokeUnit);
                 invokePreMap.put(invokeUnit, invokePreUnit);
                 units.remove(invokeUnit);
             }
         }
         ClassUtil.saveClass(sootClass, checker.getDir(), this.sootClass.getShortName());
         if(!checker.bugChecker(originOutput, sootClass.getName(), pOtions, new String[1], testtype)){
             restore(sootMethod, invokeReplaceMap, invokePreMap, originValueMap);
             ClassUtil.saveClass(sootClass, checker.getDir(), this.sootClass.getShortName());
         }else {
             recursReduce(sootMethod.getSignature());
             ClassUtil.saveClass(sootClass, ReduceChain.tmp, sootClass.getShortName());
         }
     }

    public void restore(SootMethod sootMethod, Map<Unit, Unit> invokeReplaceMap, Map<Unit, Unit> invokePreMap, Map<Unit, Value> originValueMap){
         List<MethodInvokeInfo> methodInvokeInfoList = invokeMap.get(sootMethod);
         Collections.reverse(methodInvokeInfoList);
         for(MethodInvokeInfo methodInvokeInfo : methodInvokeInfoList){
            SootMethod parentMethod = methodInvokeInfo.parentMethod;
            UnitPatchingChain units = parentMethod.retrieveActiveBody().getUnits();
            Unit invokeUnit = methodInvokeInfo.invokeUnit;
            if(invokeUnit instanceof JAssignStmt){
                ((JAssignStmt) invokeUnit).rightBox.setValue(originValueMap.get(invokeUnit));
            }else {
                Unit invokePreUnit = invokePreMap.get(invokeUnit);
                Unit invokeReplaceUnit = invokeReplaceMap.get(invokeUnit);

                for(Unit src : targetUnitMap.getOrDefault(invokeUnit, new HashSet<>())){
                    if(src instanceof JIfStmt){
                        ((JIfStmt) src).setTarget(invokeUnit);
                    }else if(src instanceof JGotoStmt){
                        ((JGotoStmt) src).setTarget(invokeUnit);
                    }
                }

                if(invokePreUnit == null){
                    if(invokeReplaceUnit != null){
                        units.removeFirst();
                    }
                    units.insertBefore(invokeUnit, units.getFirst());
                }else {
                    if(invokeReplaceUnit != null){
                        units.remove(invokeReplaceUnit);
                    }
                    units.insertAfter(invokeUnit, invokePreUnit);
                }
            }
        }
    }

    public void recursReduce(String methodName){
        sootClass.getMethods().removeIf(m -> m.getSignature().equals(methodName));
        reducedSet.add(methodName);
        List<SootMethod> children = childMap.getOrDefault(methodName, new ArrayList<>());
        for(SootMethod method : children){
            List<SootMethod> parents = parentMap.getOrDefault(method.getSignature(), new ArrayList<>());
            parents.remove(method);
            if(parents.size() == 0) {   // 該方法沒有父節點
                recursReduce(method.getSignature());
            }
        }
    }

}
