#!/bin/sh

#set JAVA_HOME to Java JDK 1.6 or later
JAVA_HOME=
export JAVA_HOME
if [ "$JAVA_HOME" = "" ]
then
	echo "JAVA_HOME must be set to Java JDK version 1.6 or later"
else
${JAVA_HOME}/bin/java -version
echo "Check Java version above is 1.6 or later"

WORKDIR=`dirname $0`
export WORKDIR
CLASSPATH=$WORKDIR:$WORKDIR/michide.jar:$WORKDIR/dtdparser.jar:$WORKDIR/javacc.jar:$WORKDIR/jh.jar:$WORKDIR/jsearch.jar:$WORKDIR/resolver.jar:$WORKDIR/itext-1.02b.jar:$WORKDIR/jhall.jar:$WORKDIR/jlfgr-1_0.jar
export CLASSPATH
${JAVA_HOME}/bin/java -Xms32m -Xmx512m com.nubean.michlic.MichiganLauncher $1
fi
