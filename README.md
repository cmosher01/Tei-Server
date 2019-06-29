# Tei-Server

TEI database web server.

Digital Archives (TEI format) server. Small and simple for now.

HTTP server accepts requests for TEI files (in the current default
directory), or list of nested directories/files. Translates TEI
(using TEISH XSLT and CSS) into HTML.

# Security

By default, this server will not serve any files. Any files that
are to be served publicly must be indicated in a file named

```
.SERVE_PUBLIC.globs
```

located in the root directory of the directory tree to be served.
Each line of the file contains a file-glob of the path to serve.
For example, to serve every file in the tree rooted at that directory:

```
**
```

Other examples:

```
foobar.tei
path/to/files/*
path/to/tree/**
```
