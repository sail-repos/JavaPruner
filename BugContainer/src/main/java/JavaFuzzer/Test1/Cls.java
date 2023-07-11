package JavaFuzzer.Test1;

import JavaFuzzer.FuzzerUtils;

class Cls {

    public static final int N = 100;

    public static long instanceCount= FuzzerUtils.nextLong();
    public int iFld=FuzzerUtils.nextInt();
    public static double dFld=FuzzerUtils.nextDouble();
    public int iArrFld[]=new int[N];

}