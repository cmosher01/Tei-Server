# TEI Server

Copyright © 2018–2021, Christopher Alan Mosher, Shelton, Connecticut, USA, <cmosher01@gmail.com>.

[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=CVSSQ2BWDCKQ2)
[![License](https://img.shields.io/github/license/cmosher01/Tei-Server.svg)](https://www.gnu.org/licenses/gpl.html)

# Archived

This project is now archived. The TEI server functionality has been
folded into the generic [xml-servlet](https://github.com/cmosher01/xml-servlet)
project.

---

# Introduction

TEI database web server.

Digital Archives ([TEI format](https://tei-c.org/)) server. Small and simple.

The HTTP server accepts requests for TEI files (from the tree rooted at
the current default directory), and converts them to XHTML5 web pages,
using XSLT, and CSS.

Only files of type `.xml` or `.tei` will be recognized.

# Security

By default, this server will not serve any files publicly. Any files that
are to be served publicly must be indicated in a file named

```
.SERVE_PUBLIC.globs
```

located in the current default directory (the root of tree to be served).
Each line of the file contains a file-glob of the path to serve publicly.
For example:
```
foobar.tei
path/to/files/*
path/to/tree/**
```

To serve every file in the tree rooted at the current default directory:
```
**
```

# Run with Docker

The easiest way to run TEI Server is with Docker. For example:

```sh
docker run -d -v "/srv/tei:/home/user:ro" -p "8080:8080" cmosher01/tei-server
```

The browse to `http://localhost:8080/`.
