package JavaFuzzer.Test3;

import JavaFuzzer.FuzzerUtils;

public class Test {

    public static final int N = 100;

    public static long instanceCount=FuzzerUtils.nextLong();
    public static int iFld=FuzzerUtils.nextInt();
    public static boolean bArrFld[]=new boolean[N];

    static {
        FuzzerUtils.init(Test.bArrFld, FuzzerUtils.nextBoolean());
    }

    public static long vMeth_check_sum = 0;
    public static long bMeth_check_sum = 0;

    public static boolean bMeth(float f1) {

        float f2=FuzzerUtils.nextFloat();
        int i8=FuzzerUtils.nextInt(), i9=FuzzerUtils.nextInt(), iArr[]=new int[N];
        long l2=FuzzerUtils.nextLong(), lArr1[]=new long[N];
        byte byArr[]=new byte[N];
        short sArr[]=new short[N];
        Cls O=new Cls();

        FuzzerUtils.init(lArr1, FuzzerUtils.nextLong());
        FuzzerUtils.init(byArr, FuzzerUtils.nextByte());
        FuzzerUtils.init(O.fArrFld, FuzzerUtils.nextFloat());
        FuzzerUtils.init(iArr, FuzzerUtils.nextInt());
        FuzzerUtils.init(sArr, FuzzerUtils.nextShort());

        f2 -= 8449;
        try {
            for (i8 = 1; i8 < 68; i8++) {
                byte by2=FuzzerUtils.nextByte();
                by2 += (byte)18934;
            }
        }
        catch (UserDefinedExceptionTest exc7) {
            for (long l1 : lArr1) {
                Test.bArrFld[(-31936 >>> 1) % N] = true;
            }
            l2 &= 26947;
        }
        finally {
            iArr = FuzzerUtils.int1array(N, (int)212);
        }
        long meth_res = Float.floatToIntBits(f1) + Float.floatToIntBits(f2) + i8 + i9 + l2 +
                FuzzerUtils.checkSum(lArr1) + FuzzerUtils.checkSum(byArr) +
                Double.doubleToLongBits(FuzzerUtils.checkSum(O.fArrFld)) + FuzzerUtils.checkSum(iArr) +
                FuzzerUtils.checkSum(sArr) + FuzzerUtils.checkSum(O);
        bMeth_check_sum += meth_res;
        return meth_res % 2 > 0;
    }

    public static void vMeth(boolean b1) {

        int i5=FuzzerUtils.nextInt(), i6=FuzzerUtils.nextInt(), i7=FuzzerUtils.nextInt();
        double d2=FuzzerUtils.nextDouble();
        float f4=FuzzerUtils.nextFloat(), f5=FuzzerUtils.nextFloat();

        for (i5 = 1; i5 < 58; i5++) {
            byte by4=FuzzerUtils.nextByte();
            for (d2 = 1; d2 < i5; ++d2) {
                if (bMeth(2.733F)) break;
            }
            f4 /= 0.48F;
            i6 += (-19727 + (i5 * i5));
            f5 %= 4298;
            by4 /= (byte)-89L;
        }
        vMeth_check_sum += (b1 ? 1 : 0) + i5 + i6 + Double.doubleToLongBits(d2) + i7 + Float.floatToIntBits(f4) +
                Float.floatToIntBits(f5);
    }

    public void mainTest(String[] strArr1) {

        if (strArr1.length > 0) FuzzerUtils.seed(77554625 + Long.parseLong(strArr1[0]));
        int i=FuzzerUtils.nextInt(), i1=FuzzerUtils.nextInt(), i2=FuzzerUtils.nextInt(), i3=FuzzerUtils.nextInt(),
                i4=FuzzerUtils.nextInt();
        long l=FuzzerUtils.nextLong(), lArr[]=new long[N];
        byte by=FuzzerUtils.nextByte();
        double d=FuzzerUtils.nextDouble(), d1=FuzzerUtils.nextDouble(), dArr[][]=new double[N][N];
        boolean b=FuzzerUtils.nextBoolean(), b2=FuzzerUtils.nextBoolean();
        short s=FuzzerUtils.nextShort();
        float f=FuzzerUtils.nextFloat();

        FuzzerUtils.init(dArr, FuzzerUtils.nextDouble());
        FuzzerUtils.init(lArr, FuzzerUtils.nextLong());

        for (i = 4; i < 69; i += 3) {
            for (i2 = i; i2 < 90; i2++) {
                byte by1=FuzzerUtils.nextByte();
                l = by;
                try {
                    by >>>= (byte)17469;
                    if (false) {
                        d /= -105;
                    } else if (b) {
                        dArr = FuzzerUtils.double2array(N, (double)0.97359);
                    }
                }
                catch (ArrayIndexOutOfBoundsException exc8) {
                    f += (((i2 * by1) + Test.instanceCount) - s);
                }
                catch (NegativeArraySizeException exc9) {
                    s += (short)-31;
                    l += (long)(lArr[i - 1] - (--d));
                    d1 += (-5 / ((long)(f) | 1));
                    Test.iFld += (i2 * i2);
                }
                s += (short)(i2 | s);
            }
        }
        if (FuzzerUtils.seed % 3 == 2) vMeth(b2);

        FuzzerUtils.out.println("i i1 i2 = " + i + "," + i1 + "," + i2);
        FuzzerUtils.out.println("i3 l by = " + i3 + "," + l + "," + by);
        FuzzerUtils.out.println("i4 d b = " + i4 + "," + Double.doubleToLongBits(d) + "," + (b ? 1 : 0));
        FuzzerUtils.out.println("s d1 f = " + s + "," + Double.doubleToLongBits(d1) + "," + Float.floatToIntBits(f));
        FuzzerUtils.out.println("b2 dArr lArr = " + (b2 ? 1 : 0) + "," +
                Double.doubleToLongBits(FuzzerUtils.checkSum(dArr)) + "," + FuzzerUtils.checkSum(lArr));
        FuzzerUtils.out.println("Cls = " + Cls.instanceCount);

        FuzzerUtils.out.println("Test.instanceCount Test.iFld Test.bArrFld = " + Test.instanceCount + "," + Test.iFld +
                "," + FuzzerUtils.checkSum(Test.bArrFld));
        FuzzerUtils.out.println("Cls = " + Cls.instanceCount);

        FuzzerUtils.out.println("bMeth_check_sum: " + bMeth_check_sum);
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