package util;

public class Debug {
    public static boolean DEBUG = false;

    public static void println(Object input) {
        if (DEBUG) {
            System.out.println(input);
        }
    }
}
