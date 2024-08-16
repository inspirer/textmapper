---
layout: default
title: Textmapper &middot; Download
kind: download
---

Installation
============

Assuming the Go toolchain is installed (see https://go.dev/doc/install),
execute the following command to download and install the latest build:

    $ go install github.com/inspirer/textmapper/cmd/textmapper@main

You can also install a VS Code extension for syntax highlighting, navigation,
and continuous grammar analysis (or simply search for 'textmapper' in the
Marketplace).

    $ code --install-extension inspirer.textmapper-support

Then go to settings, and set 'Textmapper: Path' to point to the location
of the installed binary. This can also be done in settings.json:

    {
        "textmapper.path": ".../go/bin/textmapper",
    }


Building from Sources
=====================

Clone and build the repository:

    $ git clone https://github.com/inspirer/textmapper.git
    $ cd textmapper
    $ go test ./...
    $ go install ./cmd/textmapper

Regenerating build-in grammars:

    $ ./regen.sh

Regenerating and testing C++ examples:

    $ cd cpp
    $ ./gen.sh
    $ bazel test //...
