package JavaFuzzer.Test4;

import JavaFuzzer.FuzzerUtils;

public class Test {

    public static final int N = 100;

    public static long instanceCount=FuzzerUtils.nextLong();
    public static double dFld=FuzzerUtils.nextDouble();
    public double dFld1=FuzzerUtils.nextDouble();
    public float fFld=FuzzerUtils.nextFloat();
    public int iFld1=FuzzerUtils.nextInt();
    public int iFld2=FuzzerUtils.nextInt();
    public int iFld3=FuzzerUtils.nextInt();
    public boolean bFld=FuzzerUtils.nextBoolean();
    public volatile boolean bFld1=FuzzerUtils.nextBoolean();
    public long lArrFld[][]=new long[N][N];
    public short sArrFld1[]=new short[N];
    public static short sArrFld2[]=new short[N];
    public static boolean bArrFld[]=new boolean[N];
    public int iArrFld1[]=new int[N];
    public int iArrFld3[]=new int[N];

    static {
        FuzzerUtils.init(Test.sArrFld2, FuzzerUtils.nextShort());
        FuzzerUtils.init(Test.bArrFld, FuzzerUtils.nextBoolean());
    }

    public void mainTest(String[] strArr1) {

        if (strArr1.length > 0) FuzzerUtils.seed(76075305 + Long.parseLong(strArr1[0]));
        int i=FuzzerUtils.nextInt(), i1=FuzzerUtils.nextInt(), i2=FuzzerUtils.nextInt(), i3=FuzzerUtils.nextInt(),
                i4=FuzzerUtils.nextInt(), i5=FuzzerUtils.nextInt(), i6=FuzzerUtils.nextInt(), i7=FuzzerUtils.nextInt(),
                i8=FuzzerUtils.nextInt(), i9=FuzzerUtils.nextInt(), i11=FuzzerUtils.nextInt(), i12=FuzzerUtils.nextInt(),
                i13=FuzzerUtils.nextInt();
        long l=FuzzerUtils.nextLong(), l1=FuzzerUtils.nextLong(), l2=FuzzerUtils.nextLong(), l3=FuzzerUtils.nextLong(),
                l4=FuzzerUtils.nextLong(), lArr[]=new long[N], lArr1[]=new long[N];
        short s=FuzzerUtils.nextShort(), s1=FuzzerUtils.nextShort(), s2=FuzzerUtils.nextShort(),
                s4=FuzzerUtils.nextShort(), s5=FuzzerUtils.nextShort();
        double d1=FuzzerUtils.nextDouble(), d2=FuzzerUtils.nextDouble(), d3=FuzzerUtils.nextDouble(),
                d4=FuzzerUtils.nextDouble(), dArr[][]=new double[N][N];
        byte by=FuzzerUtils.nextByte(), by1=FuzzerUtils.nextByte();
        float f=FuzzerUtils.nextFloat(), f1=FuzzerUtils.nextFloat(), f3=FuzzerUtils.nextFloat(), fArr[]=new float[N],
                fArr1[]=new float[N];
        boolean b=FuzzerUtils.nextBoolean(), b1=FuzzerUtils.nextBoolean(), b2=FuzzerUtils.nextBoolean(),
                b3=FuzzerUtils.nextBoolean(), b4=FuzzerUtils.nextBoolean(), bArr[]=new boolean[N], bArr1[]=new boolean[N];
        Cls O=new Cls();
        Cls1 O1=new Cls1();

        FuzzerUtils.init(O.byArrFld, FuzzerUtils.nextByte());
        FuzzerUtils.init(lArr, FuzzerUtils.nextLong());
        FuzzerUtils.init(O1.sArrFld, FuzzerUtils.nextShort());
        FuzzerUtils.init(fArr, FuzzerUtils.nextFloat());
        FuzzerUtils.init(bArr, FuzzerUtils.nextBoolean());
        FuzzerUtils.init(bArr1, FuzzerUtils.nextBoolean());
        FuzzerUtils.init(fArr1, FuzzerUtils.nextFloat());
        FuzzerUtils.init(O1.iArrFld2, FuzzerUtils.nextInt());
        FuzzerUtils.init(dArr, FuzzerUtils.nextDouble());
        FuzzerUtils.init(lArr1, FuzzerUtils.nextLong());

        for (i = 1; 54 > i; i++) {
            double d=FuzzerUtils.nextDouble();
            O.byArrFld[i] -= (byte)d;
        }
        for (l = 2; l < 63; ++l) {
            short s3=FuzzerUtils.nextShort();
            float f2=FuzzerUtils.nextFloat();
            int i10=FuzzerUtils.nextInt();
            try {
                s *= (short)29560;
                lArrFld[(int)(l + 1)] = FuzzerUtils.long1array(N, (long)-53267L);
                lArr = lArrFld[(int)(l - 1)];
                Test.dFld /= 1;
                try {
                    switch ((int)(l + 125)) {
                        case 125:
                            for (d1 = 1; (l + 100) > d1; ++d1) {
                                s += (short)-3;
                            }
                            for (i4 = 1; i4 < (l + 100); i4++) {
                                s1 <<= by;
                                Cls1.iFld ^= -8;
                            }
                        case 126:
                            s2 -= s1;
                        case 127:
                            dFld1 = 4788;
                            break;
                        case 128:
                            if (false) break;
                            break;
                        case 129:
                            Cls1.instanceCount |= by;
                            break;
                        case 130:
                            Test.instanceCount -= i3;
                            break;
                        case 131:
                            O1.sArrFld[(int)(l)] %= (short)((long)(d1) | 1);
                            break;
                        case 132:
                            fFld += (float)-2.47745;
                            break;
                        case 133:
                            lArrFld[(int)(l)][(int)(l + 1)] %= 4156746836L;
                            break;
                        case 134:
                            O1.sArrFld[(int)(l - 1)] = (short)-46;
                        case 135:
                            dFld1 = -21194;
                            break;
                        case 136:
                            f %= (float)-1.10630;
                            break;
                        case 137:
                            i1 -= (int)1.276F;
                        case 138:
                        case 139:
                            fArr[(int)(l - 1)] /= 24;
                            break;
                        case 140:
                            i5 += (int)(((l * Cls1.fFld1) + l1) - l2);
                            break;
                        case 141:
                            bArr = bArr;
                            break;
                        case 142:
                            sArrFld1[(int)(l + 1)] = (short)Test.instanceCount;
                            break;
                        case 143:
                            fFld /= (l3 | 1);
                            break;
                        case 144:
                            TestClassTest obj = null;
                            i5 = obj.field;
                        case 145:
                            Cls1.instanceCount %= ((long)(d2) | 1);
                            break;
                        case 146:
                            Cls1.instanceCount = -505145222L;
                        case 147:
                            iFld1 = i6;
                        case 148:
                            b = b1;
                            break;
                        case 149:
                            bArr1[(int)(l)] = b2;
                            break;
                        case 150:
                            fArr = fArr1;
                            break;
                        case 151:
                            O1.sArrFld[(int)(l + 1)] *= (short)95.125937;
                            break;
                        case 152:
                            TestClassTest obj1 = null;
                            iFld2 = obj1.field;
                            break;
                        case 153:
                            f += 52588L;
                            break;
                        case 154:
                            b2 = false;
                        case 155:
                            sArrFld1 = Test.sArrFld2;
                            break;
                        case 156:
                            d3 += 75;
                        case 157:
                            i1 += (int)(l ^ i7);
                            break;
                        case 158:
                            Test.bArrFld[(int)(l + 1)] = true;
                            break;
                        case 159:
                            s = (short)-20;
                            break;
                        case 160:
                            l4 *= by;
                            break;
                        case 161:
                            d2 /= ((long)(f1) | 1);
                            break;
                        case 162:
                            Cls1.iArrFld = iArrFld1;
                            break;
                        case 163:
                            b3 = b1;
                            break;
                        case 164:
                            by += (byte)l;
                            break;
                        case 165:
                            by %= (byte)-86.579F;
                        case 166:
                            by >>= (byte)l4;
                        case 167:
                            bArr1[(int)(l - 1)] = b3;
                            break;
                        case 168:
                            s3 *= (short)-53324L;
                        case 169:
                            Test.instanceCount += (44 + (l * l));
                            break;
                        case 170:
                            i7 = iArrFld1[46];
                            break;
                        case 171:
                            O1.iArrFld2[39517] = 12;
                            break;
                        case 172:
                            iFld2 += (int)(l + fFld);
                        case 173:
                            Test.instanceCount >>= 14415;
                        case 174:
                            by += (byte)(((l * i1) + by1) - s4);
                            break;
                        case 175:
                            TestClassTest obj2 = null;
                            synchronized(obj2) {
                                by1 /= (byte)90.129558;
                            }
                        case 176:
                            iFld2 >>= (int)48441L;
                        case 177:
                            Cls1.fFld1 = s5;
                        case 178:
                            f2 *= (float)d1;
                        case 179:
                            f %= 4796042910053585538L;
                            break;
                        case 180:
                            fFld += (l * l);
                            break;
                        case 181:
                            dArr[(int)(l - 1)] = FuzzerUtils.double1array(N, (double)-2.109343);
                        case 182:
                            b3 = b4;
                            break;
                        case 183:
                            f3 -= 20;
                            break;
                        case 184:
                            b1 = true;
                            break;
                        case 185:
                            try {
                                iFld2 = (i8 % i9);
                                iArrFld3[(int)(l)] = (24086870 / i4);
                                iFld3 = (i10 / i7);
                            } catch (ArithmeticException a_e) {}
                        case 186:
                            i7 = O1.iArrFld2[i11];
                            break;
                        case 187:
                            by += (byte)(20 + (l * l));
                        case 188:
                            bFld = true;
                            break;
                        case 189:
                            f2 -= (float)d4;
                        case 190:
                            Cls1.fFld1 += (l * l);
                            break;
                        case 191:
                            fArr = FuzzerUtils.float1array(N, (float)2.219F);
                            break;
                        case 192:
                            bFld1 = false;
                            break;
                        case 193:
                            i12 /= (int)(i13 | 1);
                            break;
                        case 194:
                            l4 /= -23199;
                            break;
                    }
                }
                catch (NullPointerException exc8) {
                }
                catch (ArrayIndexOutOfBoundsException exc9) {
                    lArr1[(int)(l)] = -104;
                }
                finally {
                    dFld1 %= 72;
                }
            }
            catch (ArrayIndexOutOfBoundsException exc10) {
                d4 *= -8017530538195368977L;
            }
        }

        FuzzerUtils.out.println("i i1 l = " + i + "," + i1 + "," + l);
        FuzzerUtils.out.println("i2 s d1 = " + i2 + "," + s + "," + Double.doubleToLongBits(d1));
        FuzzerUtils.out.println("i3 i4 i5 = " + i3 + "," + i4 + "," + i5);
        FuzzerUtils.out.println("s1 by s2 = " + s1 + "," + by + "," + s2);
        FuzzerUtils.out.println("f l1 l2 = " + Float.floatToIntBits(f) + "," + l1 + "," + l2);
        FuzzerUtils.out.println("l3 d2 i6 = " + l3 + "," + Double.doubleToLongBits(d2) + "," + i6);
        FuzzerUtils.out.println("b b1 b2 = " + (b ? 1 : 0) + "," + (b1 ? 1 : 0) + "," + (b2 ? 1 : 0));
        FuzzerUtils.out.println("d3 i7 l4 = " + Double.doubleToLongBits(d3) + "," + i7 + "," + l4);
        FuzzerUtils.out.println("f1 b3 by1 = " + Float.floatToIntBits(f1) + "," + (b3 ? 1 : 0) + "," + by1);
        FuzzerUtils.out.println("s4 s5 b4 = " + s4 + "," + s5 + "," + (b4 ? 1 : 0));
        FuzzerUtils.out.println("f3 i8 i9 = " + Float.floatToIntBits(f3) + "," + i8 + "," + i9);
        FuzzerUtils.out.println("i11 d4 i12 = " + i11 + "," + Double.doubleToLongBits(d4) + "," + i12);
        FuzzerUtils.out.println("i13 O.byArrFld lArr = " + i13 + "," + FuzzerUtils.checkSum(O.byArrFld) + "," +
                FuzzerUtils.checkSum(lArr));
        FuzzerUtils.out.println("O1.sArrFld fArr bArr = " + FuzzerUtils.checkSum(O1.sArrFld) + "," +
                Double.doubleToLongBits(FuzzerUtils.checkSum(fArr)) + "," + FuzzerUtils.checkSum(bArr));
        FuzzerUtils.out.println("bArr1 fArr1 O1.iArrFld2 = " + FuzzerUtils.checkSum(bArr1) + "," +
                Double.doubleToLongBits(FuzzerUtils.checkSum(fArr1)) + "," + FuzzerUtils.checkSum(O1.iArrFld2));
        FuzzerUtils.out.println("dArr lArr1 O = " + Double.doubleToLongBits(FuzzerUtils.checkSum(dArr)) + "," +
                FuzzerUtils.checkSum(lArr1) + "," + FuzzerUtils.checkSum(O));
        FuzzerUtils.out.println("O1 Cls Cls1 = " + FuzzerUtils.checkSum(O1) + "," + Cls.instanceCount + "," +
                Cls1.instanceCount);

        FuzzerUtils.out.println("Test.instanceCount Test.dFld dFld1 = " + Test.instanceCount + "," +
                Double.doubleToLongBits(Test.dFld) + "," + Double.doubleToLongBits(dFld1));
        FuzzerUtils.out.println("fFld iFld1 iFld2 = " + Float.floatToIntBits(fFld) + "," + iFld1 + "," + iFld2);
        FuzzerUtils.out.println("iFld3 bFld bFld1 = " + iFld3 + "," + (bFld ? 1 : 0) + "," + (bFld1 ? 1 : 0));
        FuzzerUtils.out.println("lArrFld sArrFld1 Test.sArrFld2 = " + FuzzerUtils.checkSum(lArrFld) + "," +
                FuzzerUtils.checkSum(sArrFld1) + "," + FuzzerUtils.checkSum(Test.sArrFld2));
        FuzzerUtils.out.println("Test.bArrFld iArrFld1 iArrFld3 = " + FuzzerUtils.checkSum(Test.bArrFld) + "," +
                FuzzerUtils.checkSum(iArrFld1) + "," + FuzzerUtils.checkSum(iArrFld3));
        FuzzerUtils.out.println("Cls Cls1 = " + Cls.instanceCount + "," + Cls1.instanceCount);
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