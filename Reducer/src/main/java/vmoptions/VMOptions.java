package vmoptions;

import java.util.ArrayList;
import java.util.List;

public class VMOptions {

    private String vmname;
    private List<Option> options;

    public VMOptions(String name) {
        this.vmname = name;
        options = new ArrayList<>();
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }
}
