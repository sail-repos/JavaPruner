import java.util.Locale;
import com.alibaba.fastjson.JSONArray;

public class Check {
    public Check() {
    }

    public static int checksum(int var0, Object var1) {
        if (var1 instanceof String) {
            String[] var2 = new String[]{"time", "exception", "error", "failure", "jdk", "jre", "system"};
            String[] var3 = var2;
            int var4 = var2.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String var6 = var3[var5];
                if (((String)var1).toLowerCase(Locale.ROOT).contains(var6)) {
                    return 0;
                }
            }
        }

        if (!(var1 instanceof Runnable) && !(var1 instanceof Exception)) {
            try {
                return Integer.hashCode(JSONArray.toJSON(var1).toString().hashCode());
            } catch (Exception var7) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public static int checksum(int var0, byte var1) {
        var0 += Byte.hashCode(var1);
        return Integer.hashCode(var0);
    }

    public static int checksum(int var0, short var1) {
        var0 += Short.hashCode(var1);
        return Integer.hashCode(var0);
    }

    public static int checksum(int var0, int var1) {
        var0 += Integer.hashCode(var1);
        return Integer.hashCode(var0);
    }

    public static int checksum(int var0, long var1) {
        var0 += Long.hashCode(var1);
        return Integer.hashCode(var0);
    }

    public static int checksum(int var0, float var1) {
        var0 += Float.hashCode(var1);
        return Integer.hashCode(var0);
    }

    public static int checksum(int var0, double var1) {
        var0 += Double.hashCode(var1);
        return Integer.hashCode(var0);
    }

    public static int checksum(int var0, char var1) {
        var0 += Character.hashCode(var1);
        return Integer.hashCode(var0);
    }

    public static int checksum(int var0, boolean var1) {
        var0 += Boolean.hashCode(var1);
        return Integer.hashCode(var0);
    }
}
