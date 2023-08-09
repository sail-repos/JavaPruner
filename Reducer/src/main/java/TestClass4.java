import java.io.IOException;
import java.util.prefs.InvalidPreferencesFormatException;

public class TestClass4 {
    private static void m1(String var0) throws IOException {
        m1(var0);
    }
    public static void main(String[] var0) throws IOException {
        int condition = var0.length;
        try {
            Throwable throwable = new Throwable();
            new Throwable(throwable);
            throw new InvalidPreferencesFormatException("", throwable);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void behavior1(){

    }

    public static void behavior2(){

    }

    public static void behavior3(){

    }
}
