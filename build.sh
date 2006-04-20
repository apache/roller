#!/bin/bash

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
os400=false
case "`uname`" in
CYGWIN*) cygwin=true;;
OS400*) os400=true;;
esac

CPSEP=":"
if $cygwin; then CPSEP=";"; fi

if [ "$JAVA_HOME" = "" ] ; then
	echo You must set JAVA_HOME to point to your Java JDK install directory
	exit 1
fi

rocp=${JAVA_HOME}/lib/tools.jar
rocp=${rocp}${CPSEP}./tools/buildtime/ant-1.6.2/ant.jar
rocp=${rocp}${CPSEP}./tools/buildtime/ant-1.6.2/ant-launcher.jar
rocp=${rocp}${CPSEP}./tools/buildtime/ant-1.6.2/ant-junit.jar
rocp=${rocp}${CPSEP}./tools/buildtime/ant-1.6.2/ant-commons-net.jar
rocp=${rocp}${CPSEP}./tools/buildtime/ant-1.6.2/commons-net-1.2.2.jar
rocp=${rocp}${CPSEP}./tools/buildtime/ant-1.6.2/jakarta-oro-2.0.8.jar
rocp=${rocp}${CPSEP}./tools/buildtime/junit-3.8.1.jar

echo $rocp

${JAVA_HOME}/bin/java -Xmx512m -Djava.home=${JAVA_HOME} -classpath ${rocp} org.apache.tools.ant.Main $1 $2 $3 $4 $5

