import java.util.ArrayList;
import java.util.List;

public abstract class Base {

    public abstract boolean run() throws Throwable;

    private List<Class<? extends Throwable>> classes = new ArrayList<>();
}
