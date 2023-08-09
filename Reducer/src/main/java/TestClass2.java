public class TestClass2 {
    public static void main(String[] args){
        int b;
        int a = test(2);
        dummy("dummy");
    }
    public static int test(int a){
        share(a);
        return a;
    }
    public static void opt(int a, int b){

    }
    public static void dummy(String str){
        share(str.length());
    }
    public static String share(int a){
        initResult(a);
        return "share";
    }
    public static void initResult(int a){

    }

}
