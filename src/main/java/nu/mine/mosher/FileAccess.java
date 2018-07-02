package nu.mine.mosher;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileAccess {
    private final Set<PathMatcher> allowed;

    public FileAccess(final Set<PathMatcher> allowed) {
        this.allowed = Collections.unmodifiableSet(new HashSet<>(allowed));
    }

    public static Set<PathMatcher> readPatternsFrom(final Path fileListOfGlobs) throws IOException {
        final Path globs = fileListOfGlobs.toRealPath();
        final Set<PathMatcher> allowed = Files
            .lines(globs)
            .map(globs.getParent()::resolve)
            .map(pGlob -> FileSystems.getDefault().getPathMatcher("glob:" + pGlob))
            .collect(Collectors.toSet());

        return Collections.unmodifiableSet(new HashSet<>(allowed));
    }

    public boolean allowed(final Path pathToCheck) {
        for (final PathMatcher matcher : this.allowed) {
            if (matcher.matches(pathToCheck)) {
                return true;
            }
        }
        return false;
    }
}
