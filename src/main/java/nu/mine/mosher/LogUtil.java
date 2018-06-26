package nu.mine.mosher;

public class LogUtil {
    private static final String PLACEHOLDER = "_";

    // use cleanse to guard against CWE 117: Improper Output Sanitization for Logs
    public static String cleanse(final String message) {
        return message.replaceAll("[^!-~]+", PLACEHOLDER);
    }
}
