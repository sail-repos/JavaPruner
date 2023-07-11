package SegmentationError01;

import java.util.List;
import java.util.ArrayList;

public abstract class Base {

    public abstract boolean run() throws Throwable;

    private List<Class<? extends Throwable>> classes = new ArrayList<>();
}
