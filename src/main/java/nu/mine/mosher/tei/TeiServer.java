package nu.mine.mosher.tei;

import nu.mine.mosher.security.*;
import nu.mine.mosher.xml.TeiToXhtml5;
import org.nanohttpd.protocols.http.*;
import org.nanohttpd.protocols.http.response.*;
import org.nanohttpd.util.IHandler;
import org.slf4j.*;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import static java.lang.Runtime.getRuntime;
import static org.nanohttpd.protocols.http.NanoHTTPD.*;
import static org.nanohttpd.protocols.http.response.Response.*;
import static org.nanohttpd.protocols.http.response.Status.*;

public final class TeiServer {
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.FINEST);
        LOG = LoggerFactory.getLogger(TeiServer.class);
    }

    public static void main(final String... args) throws IOException {
        LOG.info("Will serve files beneath {}", FileUtil.CWD.toString());

        final FileAccess publicAccess = new FileAccess(FileAccess.readPatternsFrom(Paths.get(".SERVE_PUBLIC.globs")));

        final IHandler<IHTTPSession, Response> handlerRequests = session -> {
            LOG.trace("--------------------------------------------------");
            try {
                return getDocument(session, publicAccess);
            } catch (final Throwable e) {
                LOG.error("Exception while processing request:", e);
                return newFixedLengthResponse(NOT_FOUND, MIME_PLAINTEXT, NOT_FOUND.getDescription());
            }
        };

        final NanoHTTPD server = new NanoHTTPD(PORT) { };
        server.setHTTPHandler(handlerRequests);
        getRuntime().addShutdownHook(new Thread(server::stop));
        server.start(SOCKET_READ_TIMEOUT, false);
    }



    private static final List<String> LIST_OF_FALSE = Collections.singletonList(Boolean.FALSE.toString());
    private static final Logger LOG;
    private static final int PORT = 8080;

    private TeiServer() {
        throw new UnsupportedOperationException();
    }

    private static Response getDocument(final IHTTPSession session, final FileAccess publicAccess) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        final String sUri = session.getUri();
        LOG.trace("Will process request for: {}", sUri);

        if (sUri.endsWith(".css")) {
            if (sUri.endsWith("/teish.css") || sUri.endsWith("/tei.css")) {
                return getTeiCss();
            }
            return getResource(sUri, "css");
        }
        if (sUri.endsWith(".ico")) {
            return getResource(sUri, "png");
        }

        final Path path = FileUtil.getRealPath(sUri);
        if (Files.isDirectory(path)) {
            if (!sUri.endsWith("/")) {
                // let's put that trailing slash on there
                String host = session.getHeaders().get("Host");
                if (host == null || host.isEmpty()) {
                    host = "tei";
                }
                return redirectPermanent("http://" + host + ":" + PORT + sUri + "/");
            }
        }

        final boolean pathAllowed = publicAccess.allowed(path);

        if (pathAllowed) {
            final boolean asTei = Boolean.parseBoolean(session.getParameters().getOrDefault("tei", LIST_OF_FALSE).get(0));
            final Document document = Files.isDirectory(path) ? buildDirectoryPage(path) : buildPage(path, asTei);
            return newFixedLengthResponse(Status.OK, document.mime(), document.document());
        }

        return unauthorized();
    }

    private static Document buildPage(final Path pathTei, final boolean asTei) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        final Document doc;
        if (asTei) {
            doc = new Document(FileUtil.readFrom(pathTei), "application/xml; charset=utf-8");
        } else {
            doc = putTeiThroughPipeline(pathTei.toUri().toURL());
        }
        return doc;
    }

    private static Document putTeiThroughPipeline(final URL urlXmlInput) throws IOException, TransformerException, ParserConfigurationException, SAXException {
        final ByteArrayOutputStream result = new ByteArrayOutputStream(2048);

        final URLConnection conn = urlXmlInput.openConnection();
        final BufferedInputStream inXml = new BufferedInputStream(conn.getInputStream());
        final BufferedOutputStream outXhtml5 = new BufferedOutputStream(result);
        TeiToXhtml5.transform(inXml, outXhtml5, true);
        outXhtml5.close();

        return new Document(result.toString(StandardCharsets.UTF_8.name()), "application/xhtml+xml; charset=utf-8");
    }

    private static Document buildDirectoryPage(final Path path) throws IOException {
        final StringBuilder body = new StringBuilder(1024);
        body.append("<article>");
        body.append("<section>");
        body.append("<ul>");
        Files
            .list(path)
            .map(FileOrFolder::new)
            .filter(FileOrFolder::show)
            .filter(f -> f.isOfType(".xml") || f.isOfType(".tei") || f.directory())
            .sorted()
            .forEach(f -> body
                .append("<li><a href=\"")
                .append(f.link())
                .append("\">")
                .append(f.name())
                .append("</a></li>"));
        body.append("</ul>");
        body.append("</section>");
        body.append("</article>");
        return new Document(htmlPage(path.getFileName().toString(), body.toString()), MIME_HTML);
    }

    private static Response unauthorized() {
        final Response authRequest = newFixedLengthResponse(UNAUTHORIZED, MIME_HTML,
            htmlPage("Unauthorized",
                "<p>Access to this document is denied.</p>" +
                    "<p><a href=\"javascript:history.back()\">\u21E6 Back</a></p>"));
        authRequest.addHeader("WWW-Authenticate", "Basic realm=\"website\"");
        return authRequest;
    }

    private static Response redirectPermanent(final String to) {
        final Response redirect = newFixedLengthResponse(REDIRECT, MIME_HTML,
            htmlPage("Redirect", "<p>Redirected to:</p><p><a href=\"" + to + "\">" + to + "</a></p>"));
        redirect.addHeader("Location", to);
        return redirect;
    }

    private static String htmlPage(final String title, final String htmlBody) {
        return
            "<!doctype html>\n" +
                "<html class=\"fontFeatures unicodeWebFonts solarizedLight\">\n" +
                "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "<meta charset=\"utf-8\">\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">\n" +
                "<title>" + title + "</title>\n" +
                "</head>\n" +
                "<body>\n\n\n" +
                htmlBody +
                "\n\n\n</body>\n" +
                "</html>\n";
    }

    private static Response getResource(final String sUri, final String mimekey) throws IOException {
        String res = "/" + sUri;
        res = res.substring(res.lastIndexOf('/') + 1);
        final InputStream stream = TeiServer.class.getClassLoader().getResourceAsStream(res);
        if (Objects.isNull(stream)) {
            return newFixedLengthResponse(NOT_FOUND, MIME_PLAINTEXT, NOT_FOUND.getDescription());
        }
        final byte[] bytes = stream.readAllBytes();
        final ByteArrayInputStream inRes = new ByteArrayInputStream(bytes);
        return newFixedLengthResponse(Status.OK, mimeTypes().get(mimekey), inRes, bytes.length);
    }

    private static Response getTeiCss() throws IOException {
        return newFixedLengthResponse(Status.OK, mimeTypes().get("css"), TeiToXhtml5.getCss());
    }
}
