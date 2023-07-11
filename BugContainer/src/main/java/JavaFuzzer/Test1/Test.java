package JavaFuzzer.Test1;

import JavaFuzzer.FuzzerUtils;

public class Test {

    public static final int N = 100;

    public static long instanceCount= FuzzerUtils.nextLong();
    public byte byFld=FuzzerUtils.nextByte();
    public static byte byArrFld[][]=new byte[N][N];
    public static Cls OFld=new Cls();

    static {
        FuzzerUtils.init(Test.byArrFld, FuzzerUtils.nextByte());
    }

    public static long vMeth_check_sum = 0;

    public void vMeth(long l) {

        long l1=FuzzerUtils.nextLong();
        float f=FuzzerUtils.nextFloat();
        int iArr[]=new int[N];

        FuzzerUtils.init(iArr, FuzzerUtils.nextInt());

        l1 = 1;
        while (++l1 < 80) {
            iArr = FuzzerUtils.int1array(N, (int)54298);
            Test.OFld.iFld *= -19988;
            f += -17036;
        }
        vMeth_check_sum += l + l1 + Test.OFld.iFld + Float.floatToIntBits(f) + FuzzerUtils.checkSum(iArr);
    }

    public void mainTest(String[] strArr1) {

        if (strArr1.length > 0) FuzzerUtils.seed(98273277 + Long.parseLong(strArr1[0]));
        long l2=FuzzerUtils.nextLong(), l3=FuzzerUtils.nextLong();
        int i=FuzzerUtils.nextInt(), i1=FuzzerUtils.nextInt(), i2=FuzzerUtils.nextInt(), i3=FuzzerUtils.nextInt(),
                i4=FuzzerUtils.nextInt(), i5=FuzzerUtils.nextInt(), i6=FuzzerUtils.nextInt();
        byte byArr[]=new byte[N];
        short sArr[]=new short[N];

        FuzzerUtils.init(Test.OFld.iArrFld, FuzzerUtils.nextInt());
        FuzzerUtils.init(byArr, FuzzerUtils.nextByte());
        FuzzerUtils.init(sArr, FuzzerUtils.nextShort());

        if (FuzzerUtils.seed % 3 == 1) vMeth(173L);
        l2 = 53;
        do {
            for (i = 3; 82 > i; i++) {
                if (false) break;
                Cls.dFld /= -18;
            }
            Test.OFld.iArrFld = FuzzerUtils.int1array(N, (int)52);
            for (i2 = 1; i2 < l2; ++i2) {
                i1 += i2;
                l3 = 1;
                do {
                    byArr = FuzzerUtils.byte1array(N, (byte)41);
                    sArr[i2 - 1] = (short)2.2F;
                } while (++l3 < 65);
                byFld /= (byte)13845;
            }
        } while (--l2 > 0);
        for (i4 = 3; 57 > i4; i4++) {
            i6 /= (int)87.996F;
            Test.byArrFld[i4] = byArr;
        }

        FuzzerUtils.out.println("l2 i i1 = " + l2 + "," + i + "," + i1);
        FuzzerUtils.out.println("i2 i3 l3 = " + i2 + "," + i3 + "," + l3);
        FuzzerUtils.out.println("i4 i5 i6 = " + i4 + "," + i5 + "," + i6);
        FuzzerUtils.out.println("Test.OFld.iArrFld byArr sArr = " + FuzzerUtils.checkSum(Test.OFld.iArrFld) + "," +
                FuzzerUtils.checkSum(byArr) + "," + FuzzerUtils.checkSum(sArr));
        FuzzerUtils.out.println("Cls = " + Cls.instanceCount);

        FuzzerUtils.out.println("Test.instanceCount byFld Test.byArrFld = " + Test.instanceCount + "," + byFld + "," +
                FuzzerUtils.checkSum(Test.byArrFld));
        FuzzerUtils.out.println("Test.OFld Cls = " + FuzzerUtils.checkSum(Test.OFld) + "," + Cls.instanceCount);

        FuzzerUtils.out.println("vMeth_check_sum: " + vMeth_check_sum);
    }
    public static void main(String[] strArr) {

        try {
            Test _instance = new Test();
            for (int i = 0; i < 20; i++ ) {
                _instance.mainTest(strArr);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            for (int i = 0; i < 50; i++ ) {
                _instance.mainTest(strArr);
            }
        } catch (Exception ex) {
            FuzzerUtils.out.println(ex.getClass().getCanonicalName());
        }
    }
}