
public class demo5 {
    public static int maxN = 100;
    public static int maxM = 50;
    public static int CHECKSUM = 0;
    public static volatile short field_42489 = -100;

    static {
        CHECKSUM = Check.checksum(CHECKSUM, CHECKSUM);
    }

    public demo5() {
    }

    public static void main(String[] var0) {
        for(int var3 = 0; var3 < maxN; CHECKSUM = Check.checksum(CHECKSUM, var3)) {
            int var1 = 0;

            while(true) {
                int var2 = maxN;
                int var10001 = var3 + var2;
                CHECKSUM = Check.checksum(CHECKSUM, var2);
                if (var1 >= var10001) {
                    ++var3;
                    break;
                }

                opt1();
                ++var1;
                CHECKSUM = Check.checksum(CHECKSUM, var1);
            }
        }

        System.out.println("CHECKSUM: " + CHECKSUM);
    }

    public static void opt1() {
        int var0 = 59;

        while(var0 < 63) {
            int var2 = 63;

            while(var2 < 75) {
                CHECKSUM = Check.checksum(CHECKSUM, field_42489 *= -2);
                if (maxM > -2) {
                    var0 += 4;
                    var2 += 4;
                }
            }
        }

    }
}
