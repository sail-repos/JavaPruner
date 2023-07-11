package JavaFuzzer.Test4;

import JavaFuzzer.FuzzerUtils;

class Cls1 {

    public static final int N = 100;

    public static long instanceCount=FuzzerUtils.nextLong();
    public static int iFld=FuzzerUtils.nextInt();
    public static float fFld1= FuzzerUtils.nextFloat();
    public volatile short sArrFld[]=new short[N];
    public static int iArrFld[]=new int[N];
    public int iArrFld2[]=new int[N];

    static {
        FuzzerUtils.init(Cls1.iArrFld, FuzzerUtils.nextInt());
    }

}