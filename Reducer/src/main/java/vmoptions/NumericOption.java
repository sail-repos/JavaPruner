package vmoptions;

public class NumericOption extends Option{
    private int max;
    private int min;
    public NumericOption(String name, int min, int max) {
        super(name);
        this.max = max;
        this.min = min;
    }
}
