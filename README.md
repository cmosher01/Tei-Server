# Tei-Server

Copyright © 2018–2019, Christopher Alan Mosher, Shelton, Connecticut, USA, <cmosher01@gmail.com>.

[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=CVSSQ2BWDCKQ2)
[![License](https://img.shields.io/github/license/cmosher01/Tei-Server.svg)](https://www.gnu.org/licenses/gpl.html)

TEI database web server.

Digital Archives (TEI format) server. Small and simple for now.

HTTP server accepts requests for TEI files (from the tree rooted at
the current default directory), and converts them to HTML web pages,
using XSLT and CSS.

The XSLT/CSS component could also be used standalone. To do this,
use `teish.xslt` to transform your TEI XML file into HTML.
Use that resulting HTML somewhere in the body of a web page
that uses `teish.css`, to style it correctly:

 * https://raw.githack.com/cmosher01/Tei-Server/master/src/main/resources/teish.xslt
 * https://raw.githack.com/cmosher01/Tei-Server/master/src/main/resources/teish.css

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
