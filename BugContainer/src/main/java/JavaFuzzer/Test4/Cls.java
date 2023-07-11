package JavaFuzzer.Test4;

import JavaFuzzer.FuzzerUtils;

class Cls extends Test {

    public static final int N = 100;

    public static volatile long instanceCount= FuzzerUtils.nextLong();
    public byte byArrFld[]=new byte[N];

}