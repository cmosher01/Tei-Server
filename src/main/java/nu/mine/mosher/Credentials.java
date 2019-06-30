package nu.mine.mosher;

import fi.iki.elonen.NanoHTTPD;
import nu.mine.mosher.security.password.HashedString;
import nu.mine.mosher.security.password.StrongHash;

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
        boolean failed = false;

        if (authorization.isEmpty()) {
            failed = true;
        }

        if (!authorization.startsWith(BASIC)) {
            failed = true;
        }

        final String base64Credentials = failed ? "" : authorization.substring(BASIC.length()); // username:password

        final String[] credentials = new String(Base64.getDecoder().decode(base64Credentials)).split(":", -1);

        String username = "";
        String password = "";
        if (credentials.length < 2) {
            failed = true;
        } else {
            username = credentials[0];
            password = credentials[1];
        }

        final String passwordHash = store.passwordFor(username);
        if (passwordHash.isEmpty()) {
            failed = true;
        }

        try {
            if (!StrongHash.isPasswordValid(password, passwordHash)) {
                failed = true;
            }
        } catch (HashedString.InvalidFormat shouldNeverHappen) {
            failed = true;
        }

        return !failed;
    }
}
