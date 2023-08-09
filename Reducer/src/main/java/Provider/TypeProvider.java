package Provider;

import soot.*;
import soot.util.Chain;

import java.util.ArrayList;
import java.util.HashMap;

public class TypeProvider {

    /**
     * Primitive type
     */
    public static IntType intType = IntType.v();
    public static BooleanType booleanType = BooleanType.v();
    public static FloatType floatType = FloatType.v();
    public static DoubleType doubleType = DoubleType.v();
    public static LongType longType = LongType.v();
    public static CharType charType = CharType.v();
    public static ShortType shortType = ShortType.v();

    public static RefType refType = RefType.v();

    public static NullType nullType = NullType.v();
    /**
     * Array type
     */
    public static ArrayType arrayType = ArrayType.v(NullType.v(), 1);

    public static ArrayList<Type> primTypes = new ArrayList<>();
    public static ArrayList<Type> refTypes = new ArrayList<>();
    public static HashMap<Type, Integer> types = new HashMap();

    static {

        primTypes.add(intType);
        primTypes.add(booleanType);
        primTypes.add(floatType);
        primTypes.add(doubleType);
        primTypes.add(longType);
        primTypes.add(charType);
        primTypes.add(shortType);
        primTypes.add(nullType);

        for (SootClass clazz: Scene.v().getClasses()) {
            if (clazz.isAbstract()) continue;
            if (!clazz.isPublic()) continue;
            if (ProviderConfig.INVALID_REFERENCE_TYPE.contains(clazz.getName()) || !clazz.getName().startsWith("java.")) continue;
            for (SootMethod method : clazz.getMethods()) {
                if (method.isPublic() && method.isConstructor()) {
                    refTypes.add(clazz.getType());
                    break;
                }
            }
        }

        types.put(intType, ProviderConfig.PROB_INT_VALUE);
        types.put(booleanType, ProviderConfig.PROB_BOOL_VALUE);
        types.put(floatType, ProviderConfig.PROB_FLOAT_VALUE);
        types.put(doubleType, ProviderConfig.PROB_DOUBLE_VALUE);
        types.put(longType, ProviderConfig.PROB_LONG_VALUE);
        types.put(charType, ProviderConfig.PROB_CHAR_VALUE);
        types.put(shortType, ProviderConfig.PROB_SHORT_VALUE);
        types.put(refType, ProviderConfig.PROB_OBJECT_VALUE);
    }
}
