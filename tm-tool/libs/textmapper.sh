#!/bin/sh

SCRIPT_LOCATION=$0
# Step through symlinks to find where the script really is
while [ -L "$SCRIPT_LOCATION" ]; do
  SCRIPT_LOCATION=`readlink -n "$SCRIPT_LOCATION"`
done

BASE=`dirname "$SCRIPT_LOCATION"`
java -cp ${BASE}/textmapper-0.10.?.jar org.textmapper.tool.Tool $*
