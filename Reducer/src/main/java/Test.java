import Reducer.ReducerContainer;

import java.io.File;

public class Test {
    public static int FIELD = 30;
    public static void main(String[] args){
        if(args.length > 2){
            test();
        }
        FIELD = FIELD - 20;
        for(int i = 0; i < 20; i++){
            FIELD = FIELD - 1;
            int a  = i / FIELD;
        }
    }

    public static void test(){

    }
}
