package examples;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class testBool {

    public static void main(String[] args) {


    }

    public static void testTryFinallyNegative(MethodHandle target, MethodHandle cleanup, String expectedMessage) {
        boolean caught = false;
        try {
//            MethodHandles.tryFinally(target, cleanup);
        } catch (IllegalArgumentException iae) {
            assertEquals(expectedMessage, iae.getMessage());
            caught = true;
        }
        assertTrue(caught);
    }
}
