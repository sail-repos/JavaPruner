package Reducer;

import java.util.HashSet;
import java.util.Set;

public class StmtInsertion {

    public static Set<String> blockPrinted = new HashSet<>();

    public static void printBlock(String pair){
        if(!blockPrinted.contains(pair)){
            System.out.println(pair);
            blockPrinted.add(pair);
        }
    }

}
