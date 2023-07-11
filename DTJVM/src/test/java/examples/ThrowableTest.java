package examples;

import org.junit.Test;

import java.lang.reflect.Field;

public class ThrowableTest {

    @Test
    public void test() throws NoSuchFieldException {

        Field f = Throwable.class.getDeclaredField("depth");

    }
}
