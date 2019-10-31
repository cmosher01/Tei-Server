package com.xmlcalabash.drivers;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.util.UserArgs;
import net.sf.saxon.s9api.SaxonApiException;

import java.io.IOException;
import java.net.URISyntaxException;

public class CalabashApi extends Main {
    public void run(final UserArgs userArgs) throws SaxonApiException, IOException, URISyntaxException {
        final XProcConfiguration config = userArgs.createConfiguration();
        run(userArgs, config);
    }
}
