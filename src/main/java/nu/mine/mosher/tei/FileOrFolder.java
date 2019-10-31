package nu.mine.mosher.tei;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

class FileOrFolder implements Comparable<FileOrFolder> {
    private final Path path;
    private final boolean directory;
    private final boolean show;
    private final String name;
    private final String nameUpper;

    public FileOrFolder(final Path path) {
        this.path = path;
        this.directory = Files.isDirectory(path);
        this.show = shouldShow(path);
        this.name = path.getFileName().toString();
        this.nameUpper = this.name.toUpperCase();
    }

    private boolean shouldShow(Path path) {
        try {
            return !Files.isHidden(path);
        } catch (IOException e) {
            return false;
        }
    }

    public Path path() {
        return this.path;
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

    public boolean isOfType(final String filetype) {
        final String filetypeUpper = filetype.toUpperCase();
        final String filetypeUpperDot = filetypeUpper.startsWith(".") ? filetypeUpper : ("."+filetypeUpper);
        return this.nameUpper.endsWith(filetypeUpperDot);
    }

    @Override
    public int compareTo(final FileOrFolder that) {
        return Comparator
            .comparing(FileOrFolder::directory)
            .thenComparing(FileOrFolder::name)
            .compare(this, that);
    }
}
