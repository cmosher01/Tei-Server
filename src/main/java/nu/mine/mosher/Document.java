package nu.mine.mosher;

public class Document {
    private final String document;
    private final String mime;

    public Document(final String document, final String mime) {
        this.document = document;
        this.mime = mime;
    }

    public String document() {
        return this.document;
    }

    public String mime() {
        return this.mime;
    }
}
