public class Test12 {

    public static char field = 'N';
    public static int CHECKSUM = 0;

    public static int maxN = 100;
    public static int maxM = 50;

    public static void main(String[] args) {

        for (int i = 0; i < maxN; i++) {
            for (int i1 = 0; i1 < i + maxM; i1++) {
                CHECKSUM = Check.checksum(CHECKSUM, field += '[');
            }
        }
        System.out.println(CHECKSUM);
    }

}
