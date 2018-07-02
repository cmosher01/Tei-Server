package nu.mine.mosher;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import javax.xml.transform.TransformerException;
import nu.mine.mosher.xml.SimpleXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.xml.sax.SAXParseException;

import static fi.iki.elonen.NanoHTTPD.Response.Status.*;
import static fi.iki.elonen.NanoHTTPD.*;
import static java.lang.Runtime.getRuntime;

public final class TeiServer {
    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    }

    private static final Logger LOG = LoggerFactory.getLogger(TeiServer.class);
    private static final int PORT = 8080;
    private static final Credentials.Store credentialsStore = GuestStoreImpl.instance();
    private static final String XML = ".xml";
    private static final String URL_TEISH_XSLT = "https://rawgit.com/cmosher01/teish/master/src/main/resources/teish.xslt";
    private static final String URL_TEISH_CSS = "https://rawgit.com/cmosher01/teish/master/src/main/resources/teish.css";
    private static final Map<String, Object> TEISH_OPTS = Map.of("full", true, "css", URL_TEISH_CSS);

    private TeiServer() {
        throw new UnsupportedOperationException();
    }

    public static void main(final String... args) throws IOException {
        LOG.info("Will serve files beneath {}", FileUtil.CWD.toString());

        final FileAccess publicAccess = new FileAccess(FileAccess.readPatternsFrom(Paths.get(".SERVE_PUBLIC.globs")));

        final NanoHTTPD server = new NanoHTTPD(PORT) {
            @Override
            public Response serve(final IHTTPSession session) {
                LOG.trace("--------------------------------------------------");
                try {
                    return getDocument(session, publicAccess);
                } catch (final Throwable e) {
                    LOG.error("Exception while processing request:", e);
                    return super.serve(session);
                }
            }
        };

        getRuntime().addShutdownHook(new Thread(server::stop));
        server.start(SOCKET_READ_TIMEOUT, false);
    }

    private static Response getDocument(final IHTTPSession session, final FileAccess publicAccess) throws IOException, SAXParseException, TransformerException {
        final Path path = FileUtil.getRealPath(session.getUri());
        if (Files.isDirectory(path)) {
            if (!session.getUri().endsWith("/")) {
                return redirectPermanent(session.getUri()+"/");
            }
        }

        if (publicAccess.allowed(path) || Credentials.fromSession(session, credentialsStore).valid()) {
            final Document document = Files.isDirectory(path) ? buildDirectoryPage(path) : buildPage(path);
            return newFixedLengthResponse(Status.OK, document.mime(), document.document());
        }

        return unauthorized();
    }

    private static Document buildPage(final Path path) throws IOException, SAXParseException, TransformerException {
        return new Document(convertTeiToHtml(FileUtil.readFrom(path)), "text/html");
    }

    private static String convertTeiToHtml(final String tei) throws SAXParseException, IOException, TransformerException {
        return new SimpleXml(tei).transform(teishXslt(), TEISH_OPTS);
    }

    private static String teishXslt() throws IOException {
        try (final InputStream in = new URL(URL_TEISH_XSLT).openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Document buildDirectoryPage(final Path path) throws IOException {
        final StringBuilder doc = new StringBuilder(256);
        doc.append("<!doctype><html><head></head><body><ul>");
        Files
            .list(path)
            .map(FileOrFolder::new)
            .filter(FileOrFolder::show)
            .filter(f -> f.isOfType(XML) || f.directory())
            .sorted()
            .forEach(f -> doc
                .append("<li><a href=\"")
                .append(f.link())
                .append("\">")
                .append(f.name())
                .append("</a></li>"));
        doc.append("</ul></body></html>");
        return new Document(doc.toString(), "text/html");
    }

    private static Response unauthorized() {
        final Response authRequest = newFixedLengthResponse(UNAUTHORIZED, MIME_PLAINTEXT, UNAUTHORIZED.getDescription());
        authRequest.addHeader("WWW-Authenticate", "Basic realm=\"website\"");
        return authRequest;
    }

    private static Response redirectPermanent(final String to) {
        final Response redirect = newFixedLengthResponse(REDIRECT, MIME_HTML,
            "<!doctype html><html><body>Redirected: <a href=\"" + to + "\">" + to + "</a></body></html>");
        redirect.addHeader("Location", to);
        return redirect;
    }
}
