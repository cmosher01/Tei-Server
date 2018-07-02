package nu.mine.mosher;

import fi.iki.elonen.NanoHTTPD;
import java.util.Base64;

public class Credentials {
    private static final String BASIC = "Basic ";

    private final boolean valid;

    private Credentials(final boolean valid) {
        this.valid = valid;
    }



    public interface Store {
        String passwordFor(String user);
    }

    public static Credentials fromSession(final NanoHTTPD.IHTTPSession session, final Store store) {
        final String authorization = session.getHeaders().getOrDefault("authorization", "");
        return new Credentials(checkValid(authorization, store));
    }

    public boolean valid() {
        return this.valid;
    }



    private static boolean checkValid(final String authorization, final Store store) {
        if (authorization.isEmpty()) {
            return false;
        }

        if (!authorization.startsWith(BASIC)) {
            return false;
        }

        final String base64Credentials = authorization.substring(BASIC.length()); // username:password

        final String[] credentials = new String(Base64.getDecoder().decode(base64Credentials)).split(":", -1);

        if (credentials.length < 2) {
            return false;
        }

        final String passwordHash = store.passwordFor(credentials[0]);
        if (passwordHash.isEmpty()) {
            return false;
        }

        if (!StrongHash.isPasswordValid(credentials[1], passwordHash)) {
            return false;
        }

        return true;
    }
}
