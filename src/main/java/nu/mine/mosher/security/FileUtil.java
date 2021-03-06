package nu.mine.mosher.security;

import org.slf4j.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.Collectors;

import static nu.mine.mosher.security.LogUtil.cleanse;

public final class FileUtil {
    public static final Path CWD = getCwd();

    public static Path getRealPath(final String uri) throws IOException {
        final String relativePath = "./" + uri;
        // below we guard against CWE-73: External Control of File Name or Path (directory traversal)
        final Path path = CWD.resolve(Paths.get(relativePath)).toAbsolutePath().normalize();
        final Path pathReal = path.toRealPath();
        if (LOG.isTraceEnabled()) {
            // use cleanse to guard against CWE 117: Improper Output Sanitization for Logs
            LOG.trace("Path (relative | normal | real): {} | {} | {}", cleanse(relativePath), cleanse(path.toString()), cleanse(pathReal.toString()));
        }
        verifyNoDirectoryTraversal(pathReal);
        verifyIsReadable(pathReal);
        return pathReal;
    }

    public static String readFrom(final Path source) throws IOException {
        return String.join("\n", Files.readAllLines(source, StandardCharsets.UTF_8));
    }

    public static String readFrom(final URL source) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(source.openConnection().getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n", "", "\n"));
        }
    }



    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    private FileUtil() {
        throw new UnsupportedOperationException();
    }

    private static Path getCwd() {
        try {
            // CWE-73: External Control of File Name or Path (directory traversal) is not a concern here:
            return Paths.get(System.getProperty("user.dir", "./")).toAbsolutePath().normalize().toRealPath();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static void verifyNoDirectoryTraversal(final Path path) throws IOException {
        if (!(path.startsWith(CWD))) {
            throw new IOException("Detected directory-traversal attempt.");
        }
    }

    private static void verifyIsReadable(final Path path) throws IOException {
        if (!Files.isReadable(path)) {
            throw new IOException("Cannot read given path: "+cleanse(path.toString()));
        }
    }
}
