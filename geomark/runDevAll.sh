#!/bin/bash

cd `dirname $0`

CLEAN=$1
export JAVA_HOME=`jenv javahome`

mvn cargo:stop
mvn -f ../jeometry/pom.xml -Dmaven.javadoc.skip=true -Dmaven.source.skip=true  $CLEAN install
mvn -f ../com.revolsys.open/pom.xml -Dmaven.javadoc.skip=true -Dmaven.source.skip=true $CLEAN install
mvn $CLEAN install
mvn cargo:start
