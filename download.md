---
layout: default
title: Textmapper &middot; Download
kind: download
---

Installation
============

Assuming the Go toolchain is installed (see https://go.dev/doc/install),
execute the following command to download and install the latest build.

    $ go install github.com/inspirer/textmapper/cmd/textmapper@main

You can also install a VS Code extension for syntax highlighting, navigation,
and continuous grammar analysis.

    $ code --install-extension inspirer.textmapper-support

Alternatively, search for 'textmapper' in the VS Code Marketplace. Once the
extension is installed, go to settings, and set 'Textmapper: Path' to point
to the location of the installed binary. This can also be done via
settings.json:

    {
        "textmapper.path": ".../go/bin/textmapper",
    }


Building from Sources
=====================

Cloning and building the repository:

    $ git clone https://github.com/inspirer/textmapper.git
    $ cd textmapper
    $ go test ./...
    $ go install ./cmd/textmapper

Regenerating built-in grammars:

    $ ./regen.sh

Regenerating and testing C++ examples:

    $ cd cpp
    $ ./gen.sh
    $ bazel test //...
