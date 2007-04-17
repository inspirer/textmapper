@echo off
xsltproc --xinclude --stringparam lang eng default.xslt index.xml > public_html\index.html
xsltproc --xinclude --stringparam lang eng default.xslt docs.xml > public_html\docs.html
xsltproc --xinclude --stringparam lang eng default.xslt samples.xml > public_html\samples.html
xsltproc --xinclude --stringparam lang eng default.xslt links.xml > public_html\links.html
xsltproc --xinclude --stringparam lang eng default.xslt download.xml > public_html\download.html
