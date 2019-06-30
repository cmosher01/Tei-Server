# Tei-Server

Copyright © 2018–2019, Christopher Alan Mosher, Shelton, Connecticut, USA, <cmosher01@gmail.com>.

[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=CVSSQ2BWDCKQ2)
[![License](https://img.shields.io/github/license/cmosher01/Tei-Server.svg)](https://www.gnu.org/licenses/gpl.html)

TEI database web server.

Digital Archives (TEI format) server. Small and simple for now.

HTTP server accepts requests for TEI files (from the tree rooted at
the current default directory), and converts them to HTML web pages,
using [TEISH XSLT and CSS](https://github.com/cmosher01/teish).

# Security

By default, this server will not serve any files. Any files that
are to be served publicly must be indicated in a file named

```
.SERVE_PUBLIC.globs
```

located in the current default directory (the root of tree to be served).
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
