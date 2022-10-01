---
layout: default
title: Textmapper &middot; Download
kind: download
---

Download
========

The latest development build (beta), 0.10.0, Sep 25, 2022:

* [Textmapper](https://github.com/inspirer/textmapper/releases/tag/0.10.0) (command-line utility)
* [IntelliJ IDEA plug-in](https://plugins.jetbrains.com/plugin/7291-textmapper)

Textmapper is currently build with Java, though it is being rewritten in Go.

Building from Sources
==================

You will need Java 8+, Ant or Maven.

    $ cd tm-tool
    $ ant clean test deploy      (OR mvn clean install)
    $ ./libs/textmapper.sh --help
