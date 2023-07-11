public class Test7 {

    public static int maxN = 100;
    public static int maxM = 50;
    public static int CHECKSUM = 0;
    public static volatile short field_2020 = -2;

    static {
        CHECKSUM = Check.checksum(CHECKSUM, CHECKSUM);
    }

    public static void main(String[] args) {

        CHECKSUM = Check.checksum(CHECKSUM, field_2020 /= 100);
        System.out.println(CHECKSUM);
    }
}
