package com.xmlcalabash.drivers;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.util.UserArgs;
import net.sf.saxon.s9api.SaxonApiException;

public class CalabashApi extends Main {
    public static class CalabashException extends Exception {
        CalabashException(Throwable cause) {
            super(cause);
        }

    }
    public void run(final UserArgs userArgs) throws CalabashException {
        try {
            final XProcConfiguration config = userArgs.createConfiguration();
            run(userArgs, config);
        } catch (final Throwable wrap) {
            throw new CalabashException(wrap);
        }
    }
}
