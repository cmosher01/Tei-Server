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
    private static final String URL_TEISH_XSLT_DEV = "https://rawgit.com/cmosher01/teish/master/src/main/resources/teish.xslt";
    private static final String URL_TEISH_XSLT_1_8 = "https://cdn.rawgit.com/cmosher01/teish/1.8/src/main/resources/teish.xslt";
    private static final String URL_TEISH_XSLT = URL_TEISH_XSLT_DEV;

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

    private static Document buildPage(final Path pathTei) throws IOException, SAXException, TransformerException, ParserConfigurationException, XMLStreamException {
        return new Document(htmlPage(convertTeiToHtml(FileUtil.readFrom(pathTei))), MIME_HTML);
    }

    private static String convertTeiToHtml(final String tei) throws SAXException, IOException, TransformerException, ParserConfigurationException, XMLStreamException {
        return "<article><section>"+new SimpleXml(XmlUnMilestone.unMilestone(tei,PB)).transform(teishXslt())+"</section></article>";
    }

    private static String teishXslt() throws IOException {
        try (final InputStream in = new URL(URL_TEISH_XSLT).openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static Document buildDirectoryPage(final Path path) throws IOException {
        final StringBuilder doc = new StringBuilder(256);
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
        final StringBuilder doc = new StringBuilder(256);
        doc.append(
            "<!doctype html>\n" +
                "<html>\n" +
                "   <head>\n" +
                "      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "      <meta charset=\"utf-8\">\n" +
                "      <link rel=\"stylesheet\" type=\"text/css\" href=\"https://mosher.mine.nu/genealogy/css/solarlt.css\">\n" + // TODO CSS
                "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/openseadragon/2.3.1/openseadragon.min.js\"></script>\n" +
                "<script type=\"text/javascript\">\n" +
                "    function seadragon(e) {\n" +
                "        var viewer = OpenSeadragon({\n" +
                "            prefixUrl: \"https://cdnjs.cloudflare.com/ajax/libs/openseadragon/2.3.1/images/\",\n" +
                "            element: e,\n" +
                "            tileSources: e.getAttribute(\"tilesources\"),\n" +
                "            maxZoomPixelRatio: 10\n" +
                "        });\n" +
                //"        viewer.addHandler(\"open\", function(){\n" +
                //"            var oldBounds = viewer.viewport.getBounds();\n" +
                //"            const imgsize = viewer.world.getItemAt(0).getContentSize();\n" +
                //"            const h = imgsize.y / imgsize.x;\n" +
                //"            var newBounds = new OpenSeadragon.Rect(0, 0, 1, h);\n" +
                //"            viewer.viewport.fitBounds(newBounds, true);\n" +
                //"            const vpbounds = viewer.viewport.getBounds(true);\n console.log(vpbounds);\n" +
                //"            viewer.viewport.panBy({x: 0, y: -vpbounds.y}, true);\n" +
                //"            console.log(viewer.viewport.getBounds(true)+'\\n\\n');\n" +
                //"        });\n" +
                "    }\n" +
                "    window.onload = () => {\n" +
                "        var i;\n" +
                "        const sds = document.querySelectorAll(\"img.tei-graphic\");\n" +
                "        for (i = 0; i < sds.length; ++i) {\n" +
                "            const img = sds[i];\n" +
                "            const url = img.getAttribute(\"url\").replace(/\\.ptif\\/.*/, \".ptif/info.json\")\n" +
                "            const div = document.createElement(\"div\");\n" +
                "            div.setAttribute(\"class\", \"tei-graphic\");\n" +
                "            div.setAttribute(\"tilesources\", url);\n" +
                "            img.parentNode.replaceChild(div, img);\n" +
                "            seadragon(div);\n" +
                "        }\n" +
                "    }\n" +
                "</script>\n" +
                "      <title></title>\n" + // TODO title
                "   </head>\n" +
                "   <body>\n\n\n" +
                htmlBody +
                "\n\n\n   </body>\n" +
                "</html>\n");
        return doc.toString();
    }
}
