package dtjvms.analyzer;

import dtjvms.executor.JIT.JvmOutput;

import java.util.HashMap;

public abstract class Filter {

    protected Filter nextFilter;

    public void setNextFilter(Filter nextFilter){
        this.nextFilter = nextFilter;
    }

    abstract protected void doFilter(HashMap<String, JvmOutput> results);
}
