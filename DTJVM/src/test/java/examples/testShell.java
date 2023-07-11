package examples;

import dtjvms.executor.DTExecutor;
import dtjvms.executor.Executor;

public class testShell {

    public static void main(String[] args) {

        Executor.getInstance().execute("./scripts/clean.sh");
    }
}
