package JavaFuzzer.Test2;

import JavaFuzzer.FuzzerUtils;

public class Test {

    public static final int N = 100;

    public static long instanceCount=FuzzerUtils.nextLong();
    public static double dFld=FuzzerUtils.nextDouble();
    public static volatile boolean bFld=FuzzerUtils.nextBoolean();
    public static double dArrFld[][]=new double[N][N];
    public double dArrFld1[][]=new double[N][N];

    static {
        FuzzerUtils.init(Test.dArrFld, FuzzerUtils.nextDouble());
    }

    public static long byMeth_check_sum = 0;
    public static long bMeth_check_sum = 0;

    public boolean bMeth(int i6) {

        int i7=FuzzerUtils.nextInt(), i9=FuzzerUtils.nextInt(), iArr1[][]=new int[N][N], iArr2[][]=new int[N][N],
                iArr3[]=new int[N];
        double d=FuzzerUtils.nextDouble();
        float fArr[]=new float[N];
        long lArr[]=new long[N];

        FuzzerUtils.init(fArr, FuzzerUtils.nextFloat());
        FuzzerUtils.init(iArr1, FuzzerUtils.nextInt());
        FuzzerUtils.init(iArr2, FuzzerUtils.nextInt());
        FuzzerUtils.init(iArr3, FuzzerUtils.nextInt());
        FuzzerUtils.init(lArr, FuzzerUtils.nextLong());

        i7 = 1;
        while (++i7 < 63) {
            int i8=FuzzerUtils.nextInt();
            d *= 30409;
            fArr[i7 + 1] += Cls1.byFld;
            iArr1 = FuzzerUtils.int2array(N, (int)-11272);
            try {
                i8 = (i8 % -20853);
                iArr2[i7][i7 + 1] = (53 / i9);
                iArr3[i7 - 1] = (-42353 % i6);
            } catch (ArithmeticException a_e) {}
            Cls1.instanceCount ^= -70;
        }
        Test.dArrFld = dArrFld1;
        long meth_res = i6 + i7 + Double.doubleToLongBits(d) + i9 + Double.doubleToLongBits(FuzzerUtils.checkSum(fArr))
                + FuzzerUtils.checkSum(iArr1) + FuzzerUtils.checkSum(iArr2) + FuzzerUtils.checkSum(iArr3) +
                FuzzerUtils.checkSum(lArr);
        bMeth_check_sum += meth_res;
        return meth_res % 2 > 0;
    }

    public byte byMeth() {

        int i2=FuzzerUtils.nextInt(), i3=FuzzerUtils.nextInt(), i4=FuzzerUtils.nextInt(), i5=FuzzerUtils.nextInt();
        long l=FuzzerUtils.nextLong();
        short s1=FuzzerUtils.nextShort();
        boolean b1=FuzzerUtils.nextBoolean();

        for (i2 = 4; i2 < 95; ++i2) {
            l *= (s1 / 34977L);
            for (i4 = i2; 88 > i4; i4++) {
                Test.dFld += -48;
            }
            if (b1) {
                b1 = (b1 = (!bMeth(6266)));
            }
        }
        long meth_res = i2 + i3 + l + s1 + i4 + i5 + (b1 ? 1 : 0);
        byMeth_check_sum += meth_res;
        return (byte)meth_res;
    }

    public void mainTest(String[] strArr1) {

        if (strArr1.length > 0) FuzzerUtils.seed(42508948 + Long.parseLong(strArr1[0]));
        int i=FuzzerUtils.nextInt(), i10=FuzzerUtils.nextInt(), i11=FuzzerUtils.nextInt(), iArr[]=new int[N];
        byte by=FuzzerUtils.nextByte(), byArr[]=new byte[N];
        boolean b=FuzzerUtils.nextBoolean();
        short s=FuzzerUtils.nextShort(), sArr[]=new short[N];
        float f=FuzzerUtils.nextFloat();

        FuzzerUtils.init(iArr, FuzzerUtils.nextInt());
        FuzzerUtils.init(sArr, FuzzerUtils.nextShort());
        FuzzerUtils.init(byArr, FuzzerUtils.nextByte());

        i = 1;
        while (++i < 65) {
            int i1=FuzzerUtils.nextInt();
            iArr[i] = by;
            by &= by;
            i1 += (int)18901L;
            switch ((i % 2) + 33) {
                case 33:
                    if (b = (Cls.bArrFld[i] = false)) break;
                    s %= (short)((long)(f) | 1);
                case 34:
                    by &= (byte)(-170 + byMeth());
                    Test.bFld = false;
                    for (short s2 : sArr) {
                        for (i10 = i; i10 < 98; i10++) {
                            Cls1.byFld -= (byte)41.38435;
                        }
                    }
                    byArr = FuzzerUtils.byte1array(N, (byte)74);
                    break;
                default:
                    s += (short)i;
                    sArr = FuzzerUtils.short1array(N, (short)32369);
            }
        }

        FuzzerUtils.out.println("i by b = " + i + "," + by + "," + (b ? 1 : 0));
        FuzzerUtils.out.println("s f i10 = " + s + "," + Float.floatToIntBits(f) + "," + i10);
        FuzzerUtils.out.println("i11 iArr sArr = " + i11 + "," + FuzzerUtils.checkSum(iArr) + "," +
                FuzzerUtils.checkSum(sArr));
        FuzzerUtils.out.println("byArr Cls Cls1 = " + FuzzerUtils.checkSum(byArr) + "," + Cls.instanceCount + "," +
                Cls1.instanceCount);

        FuzzerUtils.out.println("Test.instanceCount Test.dFld Test.bFld = " + Test.instanceCount + "," +
                Double.doubleToLongBits(Test.dFld) + "," + (Test.bFld ? 1 : 0));
        FuzzerUtils.out.println("Test.dArrFld dArrFld1 Cls = " +
                Double.doubleToLongBits(FuzzerUtils.checkSum(Test.dArrFld)) + "," +
                Double.doubleToLongBits(FuzzerUtils.checkSum(dArrFld1)) + "," + Cls.instanceCount);
        FuzzerUtils.out.println("Cls1 = " + Cls1.instanceCount);

        FuzzerUtils.out.println("bMeth_check_sum: " + bMeth_check_sum);
        FuzzerUtils.out.println("byMeth_check_sum: " + byMeth_check_sum);
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