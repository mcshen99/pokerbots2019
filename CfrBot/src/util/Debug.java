package util;

public class Debug {
    public static boolean DEBUG = true;

    public static void println(Object input) {
        if (DEBUG) {
            System.out.println(input);
        }
    }
}
