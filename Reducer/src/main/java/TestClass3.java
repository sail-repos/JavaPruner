import java.io.InvalidObjectException;
import java.util.IllegalFormatPrecisionException;
import java.util.IllformedLocaleException;

public class TestClass3 {

    static byte[] src_array;
    public static long TRAPCOUNT;
    protected static InvalidObjectException[][] field_16836;
    protected IllegalFormatPrecisionException field_16838;

    public static void main(String[] var0) {
        for(int var1 = 0; var1 < 20000; ++var1) {
            byte var10000 = test(src_array.length - 1);
            byte[] var10001 = src_array;
            field_16836 = new InvalidObjectException[128][1024];
            if (var10000 != var10001[0]) {
                throw new RuntimeException("Test failed");
            }
        }

    }

    static byte test(int var0) {
        try {
            throw new NullPointerException();
        } catch (NullPointerException var2) {
            for(int var1 = 63; var1 < 69; var1 += 3) {
                ++TRAPCOUNT;
            }

            byte[] var10000 = src_array;
            byte[] var10001 = new byte[512];
            new IllformedLocaleException();
            src_array = var10001;
            byte[] var3 = new byte[10];
            System.arraycopy(var10000, 0, var3, 1, var0);
            return var3[1];
        }
    }

}
