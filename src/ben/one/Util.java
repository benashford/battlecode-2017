package ben.one;

public class Util {
    public static final void debug_outf(String pattern, Object... args) {
        System.out.printf("%n***%n%s%n", String.format(pattern, args));
    }
}
