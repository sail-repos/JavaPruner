package dtjvms.analyzer;

import dtjvms.executor.JIT.JvmOutput;
import sun.security.krb5.internal.PAData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JDKAnalyzer extends Analyzer{

    public static JDKAnalyzer jdkAnalyzer;

    public static int RESULT_LEVEL1 = -1;
    public static int RESULT_LEVEL2 = 0;
    public static int RESULT_LEVEL3 = 1;

    public boolean discardFlag = false;
    public int resultState = 0;

    public static JDKAnalyzer getInstance(){

        if (jdkAnalyzer == null){
            jdkAnalyzer = new JDKAnalyzer();
        }
        return jdkAnalyzer;
    }

    /**
     * Return value state:
     *          -1  Exception and consistent
     *           0  Normal execute without exception
     *           1  Difference found (both exception and normal execute)
     * @param results
     * @return
     */

    @Override
    public DiffCore analysis(String className, HashMap<String, JvmOutput> results, int checkType){

        return null;
    }

    public boolean getDiscardFlag() {
        return discardFlag;
    }

    public int getResultState() {
        return resultState;
    }

    public static boolean exitValueConsistent(HashMap<String, JvmOutput> results){

        int normalExecCount = 0;
        for (Map.Entry<String, JvmOutput> result : results.entrySet()) {
            if (result.getValue().getExitValue() == 0){
                normalExecCount++;
            }
        }
        if (normalExecCount < results.keySet().size()){
            return false;
        } else if (normalExecCount == results.keySet().size()){
            return true;
        } else {
            throw new RuntimeException("This should not happen: normalExecCount (" +
                    normalExecCount +
                    ") is greater than results size (" +
                    results.keySet().size() +
                    ")");
        }
    }

    public boolean checksum(HashMap<String, JvmOutput> results, HashMap<String, JvmOutput> originOutput){
        filter(results);
        if (exitValueConsistent(results)){
            String p = "CHECKSUM:.*";
            Pattern pattern = Pattern.compile(p);

            // CHECKSUM检验
            String originDiff = "";
            String currentDiff = "";
            List<String> originList = new ArrayList<>();
            List<String> currentList = new ArrayList<>();
            for(String k : originOutput.keySet()){
                JvmOutput o = originOutput.get(k);
                JvmOutput c = results.get(k);
                String oriOutput = o.getOutput();
                String curOutput = c.getOutput();
                Matcher matcher = pattern.matcher(oriOutput);
                while(matcher.find()){
                    oriOutput = matcher.group();
                }
                if(originList.contains(oriOutput)){
                    originDiff = originDiff + String.valueOf(originList.indexOf(oriOutput));
                }else {
                    originDiff = originDiff + String.valueOf(originList.size());
                    originList.add(oriOutput);
                }

                matcher = pattern.matcher(curOutput);
                while(matcher.find()){
                    curOutput = matcher.group();
                }
                if(curOutput == null){
                    return false;
                }

                if(currentList.contains(curOutput)){
                    currentDiff = currentDiff + String.valueOf(currentList.indexOf(curOutput));
                }else {
                    currentDiff = currentDiff + String.valueOf(currentList.size());
                    currentList.add(curOutput);
                }
            }

            return currentDiff.equals(originDiff);
        }

        return false;
    }

    public boolean checkExceptionDiff(HashMap<String, JvmOutput> results){
        filter(results);
        if (!exitValueConsistent(results)){

            ArrayList<String> keys = new ArrayList<>(results.keySet());
            for (int i = 0; i < keys.size(); i++) {

                JvmOutput object1 = results.get(keys.get(i));
                for (int j = i + 1; j < keys.size(); j++){

                    JvmOutput object2 = results.get(keys.get(j));

                    //01 Errors
                    Set<String> object1Set = new HashSet<>(object1.getErrors());
                    Set<String> object2Set = new HashSet<>(object2.getErrors());
                    if (!isSetEqual(object1Set, object2Set)){
                        return false;
                    }

                    //02 Exceptions
                    object1Set = new HashSet<>(object1.getExceptions());
                    object2Set = new HashSet<>(object2.getExceptions());
                    if (!isSetEqual(object1Set, object2Set)){
                        return false;
                    }

                    //03 Failures
                    object1Set = new HashSet<>(object1.getFailures());
                    object2Set = new HashSet<>(object2.getFailures());
                    if (!isSetEqual(object1Set, object2Set)){
                        return false;
                    }
                }
            }
            resultState = RESULT_LEVEL1;
        } else {
            return true;
        }

        return true;
    }

    private boolean isSetEqual(Set<String> set1, Set<String> set2){
        if(set1.size() != set2.size()){
            return false;
        }

        for (String item1 : set1) {
            if (!set2.contains(item1)) {
                return false;
            }
        }

        return true;
    }

    public void filter(HashMap<String, JvmOutput> results){
        FilterChain filterChain = new FilterChain();

        filterChain.addFilter(new JunitFilter());
        filterChain.addFilter(new StdErrFilter());
        filterChain.addFilter(new DefErrFilter());

        filterChain.startFilter(results);
    }
}
