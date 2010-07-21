#!/bin/sh

BASE=`dirname $0`
java -cp ${BASE}/lapg-1.4.?.jar net.sf.lapg.Lapg $*
