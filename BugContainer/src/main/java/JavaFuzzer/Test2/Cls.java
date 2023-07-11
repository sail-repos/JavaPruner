package JavaFuzzer.Test2;

import JavaFuzzer.FuzzerUtils;

class Cls extends Test {

    public static final int N = 100;

    public static long instanceCount= FuzzerUtils.nextLong();
    public static boolean bArrFld[]=new boolean[N];

    static {
        FuzzerUtils.init(Cls.bArrFld, FuzzerUtils.nextBoolean());
    }

}