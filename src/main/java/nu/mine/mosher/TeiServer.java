package nu.mine.mosher;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static java.lang.Runtime.getRuntime;

class TeiServer {
    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    }

    private static final Logger LOG = LoggerFactory.getLogger(TeiServer.class);
    private static final int PORT = 8080;

    public static void main(final String... args) throws IOException {
        LOG.info("Will serve files beneath {}", FileUtil.CWD.toString());

        final NanoHTTPD server = new NanoHTTPD(PORT) {
            @Override
            public Response serve(final IHTTPSession session) {
                LOG.trace("--------------------------------------------------");
                try {
                    final Path path = FileUtil.getRealPath(session.getUri());
                    return getDocument(path);
                } catch (final Throwable e) {
                    LOG.error("Exception while processing request:", e);
                    return super.serve(session);
                }
            }
        };

        getRuntime().addShutdownHook(new Thread(server::stop));
        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    private static NanoHTTPD.Response getDocument(final Path path) throws IOException {
        if (Files.isDirectory(path)) {
            return NanoHTTPD.newFixedLengthResponse(Status.OK, "text/html", buildDirectoryPage(path));
        } else {
            final List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            final String document = String.join("\r\n", lines);
            return NanoHTTPD.newFixedLengthResponse(Status.OK, "text/html", document);
        }
    }

    private static String buildDirectoryPage(final Path path) throws IOException {
        final StringBuilder doc = new StringBuilder(256);
        doc.append("<!doctype><html><head></head><body><p>directory listing:<ul>");
        Files
            .list(path)
            .map(FileOrFolder::new)
            .filter(FileOrFolder::show)
            .sorted()
            .forEach(f -> doc.append("<li><a href=\"").append(f.link()).append("\">").append(f.name()).append("</a></li>"));
        doc.append("</ul></p></body></html>");
        return doc.toString();
    }

    private static class FileOrFolder implements Comparable<FileOrFolder> {
        private final Path path;
        private final boolean directory;
        private final boolean show;
        private final String name;

        public FileOrFolder(final Path path) {
            this.path = path;
            this.directory = Files.isDirectory(path);
            this.show = shouldShow(path);
            this.name = path.getFileName().toString();
        }

        private boolean shouldShow(Path path) {
            try {
                return !Files.isHidden(path);
            } catch (IOException e) {
                return false;
            }
        }

        public String name() {
            return this.name;
        }

        public boolean directory() {
            return this.directory;
        }

        public boolean show() {
            return this.show;
        }

        public String link() {
            return this.name+(this.directory ? "/" : "");
        }

        @Override
        public int compareTo(final FileOrFolder that) {
            return Comparator
                .comparing(FileOrFolder::directory)
                .thenComparing(FileOrFolder::name)
                .compare(this, that);
        }
    }
}
