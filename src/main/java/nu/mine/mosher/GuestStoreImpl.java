package nu.mine.mosher;

/**
 * Example Credentials.Store that has only only user, "guest",
 * with a strong password (that we only store the uncrackable hash of).
 */
final class GuestStoreImpl implements Credentials.Store {
    private static final String GUEST_USERNAME = "guest";
    private static final String GUEST_PASSWORD_HASH =
        "101021:" +
        "1068F32040220866435C989DA3A120F7:" +
        "17DB77FDEC8E7C5E348E5C435C669F206AB47A4402337FB353266DBA59375AE85ACB9C83C37E3DB0C08DF59F2449639002E27B57716EA3DB069B345B7E247968";
    private static final Credentials.Store INSTANCE = new GuestStoreImpl();

    private GuestStoreImpl() {
    }

    public static Credentials.Store instance() {
        return INSTANCE;
    }

    @Override
    public String passwordFor(final String user) {
        if (!user.equals(GUEST_USERNAME)) {
            return "";
        }

        return GUEST_PASSWORD_HASH;
    }
}
