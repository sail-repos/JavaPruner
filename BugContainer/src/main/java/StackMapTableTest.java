
public class StackMapTableTest {


    public static void main(String[] args) {
        opt();
    }

    public static void opt(){
        int var4 = 10;
        int var5 = 1;
        if (var4 > 0) {
            int c = var4 - 2;
            var5 = var4 % -2;
            if (c > 3){
                var5 = 1;
            }
        }
        System.out.println(var5);
    }
}
