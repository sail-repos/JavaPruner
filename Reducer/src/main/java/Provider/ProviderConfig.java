package Provider;

import soot.Type;
import soot.jimple.Expr;

import java.util.*;

public class ProviderConfig {

    private static int probability;

    private static long randomSeed = -1;
    private static Random random;

    public static final String SEMICOLON = ";";
    // max value configurations
    public static int MAX_FIELD_NUM = 10;
    public static int MAX_FUNS_NUM = 10;
    public static int MAX_FUNS_PARAMS = 5;
    public static int MAX_METHOD_INVOCATION = 15;

    public static int MAX_ARRAY_DIM = 5;
    public static int MAX_ARRAY_SIZE_PERDIM = 20;
    public static int MAX_ARRAY_LENGTH = 256;

    public static int MAX_FUZZ_STEP = 20;
    public static int MAX_NESTED_SIZE = 5;
    public static int MAX_BLOCK_INST = 500;
    public static int MAX_LOOP_SIZE = 100;
    public static int MAX_LOOP_STEP = 5;
    public static int MAX_SWITCH_CASES = 10;

    /**
     * group probability for Type
     */
    public static HashMap<Type, Integer> PROB_TYPE_GROUP = new HashMap<>();
    public static int PROB_CHAR_VALUE = 50;
    public static int PROB_INT_VALUE = 50;
    public static int PROB_BOOL_VALUE = 50;
    public static int PROB_FLOAT_VALUE = 50;
    public static int PROB_DOUBLE_VALUE = 50;
    public static int PROB_LONG_VALUE = 50;
    public static int PROB_SHORT_VALUE = 50;
    public static int PROB_OBJECT_VALUE = 50;

    public static int PROB_NULL_VALUE = 50;

    public static int PROB_GLOBAL_FIELD = 50;
    public static int PROB_NEW_ARRAY = 50;
    public static int PROB_REUSE_VAR = 50;
    public static int PROB_REUSE_REF_VAR = 50;

    public static int PROB_VOID_METHOD = 50;
    public static int PROB_STATIC_FIELD = 50;
    public static int PROB_VOLATILE_FIELD = 50;

    public static int PROB_RETURN_VALUE = 50;
    public static int PROB_BREAK_VALUE = 50;
    public static int PROB_GOTO_VALUE = 50;
    public static int PROB_REUSE_INST = 50;

    public static int PROB_STATIC_METHOD_INVOCATION = 50;

    public static int PROB_SELF_METHOD_INVOCATION = 60;

    public static int PROB_INITIALIZE_METHOD = 50;

    public static Set<String> INVALID_REFERENCE_TYPE = new HashSet<>();
    public static Set<String> INVALID_EXCEPTION_TYPE = new HashSet<>();

    static {

        ProviderProperties fuzzingProperties = ProviderProperties.getInstance();
        // max value configurations
        MAX_FIELD_NUM = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.MAX_FIELD_NUM_KEY));
        MAX_FUNS_NUM = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.MAX_FUNS_NUM_KEY));
        MAX_FUNS_PARAMS = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.MAX_FUNS_PARAMS_KEY));
        MAX_METHOD_INVOCATION = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.MAX_METHOD_INVOCATION_KEY));

        MAX_ARRAY_DIM = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.MAX_ARRAY_DIM_KEY));
        MAX_ARRAY_SIZE_PERDIM = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.MAX_ARRAY_SIZE_PERDIM_KEY));
        MAX_ARRAY_LENGTH = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.MAX_ARRAY_LENGTH_KEY));

        MAX_FUZZ_STEP = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.MAX_FUZZ_STEP_KEY));
        MAX_NESTED_SIZE = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.MAX_NESTED_SIZE_KEY));
        MAX_BLOCK_INST = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.MAX_BLOCK_INST_KEY));
        MAX_LOOP_SIZE = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.MAX_LOOP_SIZE_KEY));
        MAX_LOOP_STEP = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.MAX_LOOP_STEP_KEY));
        MAX_SWITCH_CASES = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.MAX_SWITCH_CASES_KEY));

        PROB_CHAR_VALUE = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_CHAR_VALUE_KEY));
        PROB_INT_VALUE = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_INT_VALUE_KEY));
        PROB_BOOL_VALUE = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_BOOL_VALUE_KEY));
        PROB_FLOAT_VALUE = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_FLOAT_VALUE_KEY));
        PROB_DOUBLE_VALUE = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_DOUBLE_VALUE_KEY));
        PROB_LONG_VALUE = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_LONG_VALUE_KEY));
        PROB_SHORT_VALUE = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_SHORT_VALUE_KEY));
        PROB_OBJECT_VALUE = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_OBJECT_VALUE_KEY));
        PROB_TYPE_GROUP.put(TypeProvider.charType, PROB_CHAR_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.intType, PROB_INT_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.booleanType, PROB_BOOL_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.floatType, PROB_FLOAT_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.doubleType, PROB_DOUBLE_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.longType, PROB_LONG_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.shortType, PROB_SHORT_VALUE);
        PROB_TYPE_GROUP.put(TypeProvider.refType, PROB_OBJECT_VALUE);

        PROB_NULL_VALUE = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_NULL_VALUE_KEY));

        PROB_GLOBAL_FIELD = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_GLOBAL_FIELD_KEY));
        PROB_NEW_ARRAY = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_NEW_ARRAY_KEY));
        PROB_REUSE_VAR = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_REUSE_VAR_KEY));
        PROB_REUSE_REF_VAR = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_REUSE_REF_VAR_KEY));

        PROB_VOID_METHOD = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_VOID_METHOD_KEY));
        PROB_STATIC_FIELD = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_STATIC_FIELD_KEY));
        PROB_VOLATILE_FIELD = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_VOLATILE_FIELD_KEY));

        PROB_RETURN_VALUE = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_RETURN_VALUE_KEY));
        PROB_BREAK_VALUE = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_BREAK_VALUE_KEY));
        PROB_GOTO_VALUE = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_GOTO_VALUE_KEY));
        PROB_REUSE_INST = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_REUSE_INST_KEY));

        PROB_STATIC_METHOD_INVOCATION = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_STATIC_METHOD_INVOCATION_KEY));
        PROB_SELF_METHOD_INVOCATION = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_SELF_METHOD_INVOCATION_KEY));

        PROB_INITIALIZE_METHOD = Integer.parseInt(fuzzingProperties.getProperty(ProviderProperties.PROB_INITIALIZE_METHOD_VALUE_KEY));

        storePropertyValues(fuzzingProperties.getProperty(ProviderProperties.INVALID_REFERENCE_TYPE_KEY), INVALID_REFERENCE_TYPE);
        storePropertyValues(fuzzingProperties.getProperty(ProviderProperties.INVALID_EXCEPTION_TYPE_KEY), INVALID_EXCEPTION_TYPE);

    }

    private static void storePropertyValues(String values, Set<String> toSet) {

        if (values != null) {
            String[] split = values.split(SEMICOLON);
            for (String val : split) {
//                val = val.replace(DOT, SLASH).trim();
                val = val.trim();
                if (!val.isEmpty()) {
                    toSet.add(val);
                }
            }
        }
    }

    public static Random getRandom() {

        if (random != null) {
            return random;
        } else {
            if (randomSeed == -1) {
                long seed = System.currentTimeMillis();
                System.err.println("seed" + seed);
                random = new Random(seed);
            } else {
                random = new Random(randomSeed);
            }
        }
        return random;
    }
}
