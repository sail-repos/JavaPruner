package Provider;

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.typing.fast.Integer127Type;
import soot.jimple.toolkits.typing.fast.Integer1Type;
import soot.jimple.toolkits.typing.fast.Integer32767Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrimitiveValueProvider {

    public static List<Integer> intPredefined = new ArrayList<>(Arrays.asList(-100, -3, -2, -1, 0, 1, 2, 3, 100));
    public static List<Float> floatPredefined = new ArrayList<>(Arrays.asList(-100f, -3.0f, -2.0f, -1.0f, -0.1f, 0.0f, 0.1f, 1.0f, 2.0f, 3.0f, 100f));
    public static List<Double> doublePredefined = new ArrayList<>(Arrays.asList(-100d, -3.0d, -2.0d, -1.0d, -0.1d, 0.0d, 0.1d, 1.0d, 2.0d, 3.0d, 100d));
    public static List<Character> charPredefined = new ArrayList<Character>(Arrays.asList(
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            '0','1','2','3','4','5','6','7','8','9',
            '~','`','!','@','#','$','%','^','&','*','(',')','-','_','+','=','{','}','[',']',':',';','\'','<','>',
            ',','.','?','/','|',' '));// all chars found on a US keyboard, except for '\' and '"' which causes trouble in Strings

    public static Value next(Type type){

        if (type instanceof BooleanType){

            if (nextBoolean()){
                return IntConstant.v(0);
            } else {
                return IntConstant.v(1);
            }
        } else if (type instanceof ByteType){
            return IntConstant.v(nextByte());
        } else if (type instanceof CharType){
            return IntConstant.v(nextChar());
        } else if (type instanceof DoubleType){
            return DoubleConstant.v(nextDouble());
        } else if (type instanceof FloatType){
            return FloatConstant.v(nextFloat());
        } else if (type instanceof IntType){
            return IntConstant.v(nextInt());
        } else if (type instanceof Integer127Type){
            return IntConstant.v(nextInt());
            //TODO
        } else if (type instanceof Integer1Type){
            return IntConstant.v(nextInt());
            //TODO
        } else if (type instanceof Integer32767Type){
            return IntConstant.v(nextInt());
            //TODO
        } else if (type instanceof LongType){
            return LongConstant.v(nextInt());
        } else if (type instanceof ShortType){
            //TODO
            return IntConstant.v(nextInt());
        } else if (type instanceof RefType){

            if ( ((RefType)type).getClassName().equals("java.lang.String") ){
                return StringConstant.v(nextString());
            } else {
                return null;
            }
        } else if (type instanceof  NullType) {
          return NullConstant.v();
        } else {
            return null;
        }
    }

    public static int nextInt(){
        return intPredefined.get(ProviderConfig.getRandom().nextInt(intPredefined.size()));
    }

    public static char nextChar(){
        return charPredefined.get(ProviderConfig.getRandom().nextInt(charPredefined.size()));
    }

    public static String nextString(){
        String str = "";
        while (ProviderConfig.getRandom().nextBoolean()){
            str += nextChar();
        }
        return str;
    }

    public static boolean nextBoolean(){
        return ProviderConfig.getRandom().nextBoolean();
    }

    public static float nextFloat(){
        return floatPredefined.get(ProviderConfig.getRandom().nextInt(floatPredefined.size()));
    }

    public static double nextDouble(){
        return doublePredefined.get(ProviderConfig.getRandom().nextInt(doublePredefined.size()));
    }

    public static byte nextByte(){
        return intPredefined.get(ProviderConfig.getRandom().nextInt(intPredefined.size())).byteValue();
    }
}
