#!/bin/sh
if [ "$JAVA_HOME" = "" ] ; then
	echo You must set JAVA_HOME to point to your Java JDK install directory
	exit 1
fi

_JDBCJAR=${CATALINA_HOME}/common/lib/mysql-connector.jar
#_JDBCJAR=${CATALINA_HOME}/common/lib/postgresql.jar
#_JDBCJAR=${CATALINA_HOME}/common/lib/hsqldb.jar
_CP=../lib/rollerbeans.jar
_CP=${_CP}:../lib/commons-logging.jar
_CP=${_CP}:${_JDBCJAR}

${JAVA_HOME}/bin/java -classpath ${_CP} org.roller.business.utils.PasswordUtility $1 $2 $3
