package nu.mine.mosher;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import nu.mine.mosher.xml.SimpleXml;
import org.slf4j.*;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

import static fi.iki.elonen.NanoHTTPD.*;
import static fi.iki.elonen.NanoHTTPD.Response.Status.*;
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
    private static final URL URL_TEISH_XSLT;
    static {
        try {
            URL_TEISH_XSLT = URI.create("https://cdn.jsdelivr.net/gh/cmosher01/teish@master/src/main/resources/teish.xslt").toURL();
        } catch (final Throwable e) {
            throw new IllegalStateException(e);
        }
    }

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

    private static final List<String> LIST_OF_FALSE = Collections.singletonList(Boolean.FALSE.toString());

    private static Response getDocument(final IHTTPSession session, final FileAccess publicAccess) throws IOException, SAXException, TransformerException {
        final String sUri = session.getUri();

        if (sUri.endsWith(".css")) {
            return getResource(sUri, "css");
        }
        if (sUri.endsWith(".ico")) {
            return getResource(sUri, "png");
        }

        final Path path = FileUtil.getRealPath(sUri);
        if (Files.isDirectory(path)) {
            if (!sUri.endsWith("/")) {
                String host = session.getHeaders().get("Host");
                if (host == null || host.isEmpty()) {
                    host = "tei";
                }
                return redirectPermanent("http://"+host+":"+PORT+sUri +"/");
            }
        }

        if (publicAccess.allowed(path) || Credentials.fromSession(session, credentialsStore).valid()) {
            final boolean asTei = Boolean.parseBoolean(session.getParameters().getOrDefault("tei", LIST_OF_FALSE).get(0));
            final Document document = Files.isDirectory(path) ? buildDirectoryPage(path) : buildPage(path, asTei);
            return newFixedLengthResponse(Status.OK, document.mime(), document.document());
        }

        return unauthorized();
    }

    private static Response getResource(String sUri, String mimekey) {
        String res = "/"+sUri;
        res = res.substring(res.lastIndexOf('/')+1);
        final InputStream inRes = TeiServer.class.getResourceAsStream("/"+res);
        return newChunkedResponse(Status.OK, mimeTypes().get(mimekey), inRes);
    }

    private static Document buildPage(final Path pathTei, final boolean asTei) throws IOException, SAXException, TransformerException {
        final Document doc;
        if (asTei) {
            doc = new Document(FileUtil.readFrom(pathTei), "application/xml; charset=utf-8");
        } else {
            doc = new Document(htmlPage(convertTeiToHtml(FileUtil.readFrom(pathTei))), MIME_HTML);
        }
        return doc;
    }

    private static String convertTeiToHtml(final String tei) throws SAXException, IOException, TransformerException {
        return "<article><section>"+new SimpleXml(tei).transform(teishXslt())+"</section></article>";
    }

    private static String teishXslt() throws IOException {
        return FileUtil.readFrom(URL_TEISH_XSLT);
    }

    private static Document buildDirectoryPage(final Path path) throws IOException {
        final StringBuilder doc = new StringBuilder(256);
        doc.append("<article>");
        doc.append("<section>");
        doc.append("<ul>");
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
        doc.append("</ul>");
        doc.append("</section>");
        doc.append("</article>");
        return new Document(htmlPage(doc.toString()), MIME_HTML);
    }

    private static Response unauthorized() {
        final Response authRequest = newFixedLengthResponse(UNAUTHORIZED, MIME_HTML,
            htmlPage(
                "<p>Access to this document is denied.</p>" +
                "<a href=\"javascript:history.back()\">\u21E6 Back</a>"));
        authRequest.addHeader("WWW-Authenticate", "Basic realm=\"website\"");
        return authRequest;
    }

    private static Response redirectPermanent(final String to) {
        final Response redirect = newFixedLengthResponse(REDIRECT, MIME_HTML,
            htmlPage("<p>Redirected to:<p><a href=\"" + to + "\">" + to + "</a>"));
        redirect.addHeader("Location", to);
        return redirect;
    }

    private static String htmlPage(final String htmlBody) {
        return
            "<!doctype html>\n" +
            "<html class=\"fontFeatures unicodeWebFonts solarizedLight\">\n" +
            "<head>\n" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
            "<meta charset=\"utf-8\">\n" +
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">\n" +
            "<title></title>\n" + // TODO title
            "</head>\n" +
            "<body>\n\n\n" +
            htmlBody +
            "\n\n\n</body>\n" +
            "</html>\n";
    }
}
