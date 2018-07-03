package nu.mine.mosher;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileAccess {
    private static final Logger LOG = LoggerFactory.getLogger(TeiServer.class);

    private final Set<PathMatcher> allowed;

    public FileAccess(final Set<PathMatcher> allowed) {
        this.allowed = Collections.unmodifiableSet(new HashSet<>(allowed));
    }

    public static Set<PathMatcher> readPatternsFrom(final Path fileListOfGlobs) throws IOException {
        final StringBuilder messageGlobs = new StringBuilder();

        final Path globs = fileListOfGlobs.toRealPath();
        final Set<PathMatcher> allowed = Files
            .lines(globs)
            .map(globs.getParent()::resolve)
            .peek(glob -> messageGlobs.append("\n").append(glob))
            .map(pGlob -> FileSystems.getDefault().getPathMatcher("glob:" + pGlob))
            .collect(Collectors.toSet());

        LOG.info("Access allowed for the following globs: {}", messageGlobs);

        return Collections.unmodifiableSet(new HashSet<>(allowed));
    }

    public boolean allowed(final Path pathToCheck) {
        final Path path;
        if (Files.isDirectory(pathToCheck)) {
            path = pathToCheck.resolve(".");
        } else {
            path = pathToCheck;
        }

        for (final PathMatcher matcher : this.allowed) {
            if (matcher.matches(path)) {
                return true;
            }
        }

        LOG.info("Access denied for: {}", path);

        return false;
    }
}
