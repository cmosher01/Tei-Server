package nu.mine.mosher;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import nu.mine.mosher.xml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.xml.sax.SAXException;

import static fi.iki.elonen.NanoHTTPD.Response.Status.*;
import static fi.iki.elonen.NanoHTTPD.*;
import static java.lang.Runtime.getRuntime;

public final class TeiServer {
    private static final TagName PB = new TagName("http://www.tei-c.org/ns/1.0", "pb", "pb");

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    }

    private static final Logger LOG = LoggerFactory.getLogger(TeiServer.class);
    private static final int PORT = 8080;
    private static final Credentials.Store credentialsStore = GuestStoreImpl.instance();
    private static final String XML = ".xml";
    private static final String URL_TEISH_XSLT = "https://cdn.jsdelivr.net/gh/cmosher01/teish@/master/src/main/resources/teish.xslt";

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

    private static Response getDocument(final IHTTPSession session, final FileAccess publicAccess) throws
        IOException,
        SAXException,
        TransformerException,
        ParserConfigurationException,
        XMLStreamException {
        final String sUri = session.getUri();

        if (sUri.endsWith(".js")) {
            return getResource(sUri, "js");
        }
        if (sUri.endsWith(".css")) {
            return getResource(sUri, "css");
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
            final boolean asTei = Boolean.valueOf(session.getParameters().getOrDefault("tei", List.of(Boolean.FALSE.toString())).get(0));
            final boolean withOsd = Boolean.valueOf(session.getParameters().getOrDefault("osd", List.of(Boolean.FALSE.toString())).get(0));
            final Document document = Files.isDirectory(path) ? buildDirectoryPage(path) : buildPage(path, asTei, withOsd);
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

    private static Document buildPage(final Path pathTei, final boolean asTei, final boolean osd)
        throws IOException, SAXException, TransformerException, ParserConfigurationException, XMLStreamException {
        final Document doc;
        if (asTei) {
            doc = new Document(FileUtil.readFrom(pathTei), "application/xml; charset=utf-8");
        } else {
            doc = new Document(htmlPage(convertTeiToHtml(FileUtil.readFrom(pathTei)), osd), MIME_HTML);
        }
        return doc;
    }

    private static String convertTeiToHtml(final String tei)
        throws SAXException, IOException, TransformerException, ParserConfigurationException, XMLStreamException {
        return "<article><section>"+new SimpleXml(XmlUnMilestone.unMilestone(tei,PB)).transform(teishXslt())+"</section></article>";
    }

    private static String teishXslt() throws IOException {
        try (final InputStream in = new URL(URL_TEISH_XSLT).openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
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
        return htmlPage(htmlBody, false);
    }

    private static String htmlPage(final String htmlBody, final boolean osd) {
        final StringBuilder doc = new StringBuilder(256);
        doc.append(
            "<!doctype html>\n" +
            "<html class=\"fontFeatures unicodeWebFonts solarizedLight\">\n" +
            "<head>\n" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
            "<meta charset=\"utf-8\">\n" +
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">\n" +
            (osd ?
                "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/openseadragon/2.3.1/openseadragon.min.js\"></script>\n" +
                "<script src=\"tei.js\"></script>\n"
            :
                ""
            ) +
            "<title></title>\n" + // TODO title
            "</head>\n" +
            "<body>\n\n\n" +
            htmlBody +
            "\n\n\n</body>\n" +
            "</html>\n");
        return doc.toString();
    }
}
