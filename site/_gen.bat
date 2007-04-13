@echo off
xsltproc --xinclude --stringparam lang eng default.xslt index.xml > public_html\index.html