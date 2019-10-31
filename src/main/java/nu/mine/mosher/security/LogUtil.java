package nu.mine.mosher.security;

public final class LogUtil {
    private static final String PLACEHOLDER = "_";

    // use cleanse to guard against CWE 117: Improper Output Sanitization for Logs
    public static String cleanse(final String message) {
        return message.replaceAll("[^!-~]+", PLACEHOLDER);
    }

    private LogUtil() {
        throw new UnsupportedOperationException();
    }
}
