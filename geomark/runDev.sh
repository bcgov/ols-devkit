#!/bin/bash

cd `dirname $0`

CLEAN=$1
export JAVA_HOME=`jenv javahome`

mvn cargo:stop
mvn $CLEAN install -Dskip.frontend=true
mvn cargo:start
