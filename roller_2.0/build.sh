#!/bin/bash
if [ "$JAVA_HOME" = "" ] ; then
	echo You must set JAVA_HOME to point to your Java JDK install directory
	exit 1
fi

rocp=${JAVA_HOME}/lib/tools.jar
rocp=${rocp}:./tools/buildtime/ant-1.6.2/ant.jar
rocp=${rocp}:./tools/buildtime/ant-1.6.2/ant-launcher.jar
rocp=${rocp}:./tools/buildtime/ant-1.6.2/ant-junit.jar
rocp=${rocp}:./tools/buildtime/ant-1.6.2/ant-commons-net.jar
rocp=${rocp}:./tools/buildtime/ant-1.6.2/commons-net-1.2.2.jar
rocp=${rocp}:./tools/buildtime/ant-1.6.2/jakarta-oro-2.0.8.jar
rocp=${rocp}:./tools/buildtime/junit-3.8.1.jar

${JAVA_HOME}/bin/java -Xmx300m -Djava.home=${JAVA_HOME} -classpath ${rocp} org.apache.tools.ant.Main $1 $2 $3 $4 $5
