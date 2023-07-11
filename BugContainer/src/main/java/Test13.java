public class Test13 {

    public static int CHECKSUM = 0;
    public static short field_4 = -17200;

    public static void main(String[] paramArrayOfString) {
        CHECKSUM = (field_4 *= -2) + (field_4 *= -2);
        System.out.println("CHECKSUM: " + CHECKSUM);
    }
}
