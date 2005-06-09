#!/bin/sh
if [ "$JAVA_HOME" = "" ] ; then
	echo You must set JAVA_HOME to point to your Java JDK install directory
	exit 1
fi
_JDBCJAR=${CATALINA_HOME}/common/lib/mysql-connector-java-3.0.15-ga-bin.jar
_CP=../lib/rollerbeans.jar
_CP=${_CP}:../lib/commons-logging.jar
_CP=${_CP}:${_JDBCJAR}

${JAVA_HOME}/bin/java -classpath ${_CP} org.roller.business.utils.ConsistencyCheck $1 $2 $3 $4 
