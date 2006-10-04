#!/bin/bash
# Executes a Roller program on the command line
#
# Takes one argument, the roller class to run
#

if [ ! "$1" ] ; then
  echo "usage: $0 <classname>"
  exit 0;
fi

JAVA_HOME=/usr/local/jdk1.5
CATALINA_HOME=/home1/r/roller
ROLLER_HOME=/home1/r/roller/public_html
ROLLER_CONFIG=${CATALINA_HOME}/conf/roller-custom.properties
export JAVA_HOME
export CATALINA_HOME
export ROLLER_HOME
export ROLLER_CONFIG

_JDBCJAR=${CATALINA_HOME}/common/lib/mysql-connector-java-3.0.11-stable-bin.jar

_CP=.:\
${ROLLER_HOME}/WEB-INF/lib/JSPWiki.jar:\
${ROLLER_HOME}/WEB-INF/lib/antlr.jar:\
${ROLLER_HOME}/WEB-INF/lib/asm-attrs.jar:\
${ROLLER_HOME}/WEB-INF/lib/asm.jar:\
${ROLLER_HOME}/WEB-INF/lib/cglib-2.1.3.jar:\
${ROLLER_HOME}/WEB-INF/lib/commons-collections.jar:\
${ROLLER_HOME}/WEB-INF/lib/commons-lang-2.0.jar:\
${ROLLER_HOME}/WEB-INF/lib/commons-logging.jar:\
${ROLLER_HOME}/WEB-INF/lib/commons-logging.jar:\
${ROLLER_HOME}/WEB-INF/lib/dom4j-1.6.1.jar:\
${ROLLER_HOME}/WEB-INF/lib/ecs.jar:\
${ROLLER_HOME}/WEB-INF/lib/ehcache-1.1.jar:\
${ROLLER_HOME}/WEB-INF/lib/hibernate3.jar:\
${ROLLER_HOME}/WEB-INF/lib/jaxen-full.jar:\
${ROLLER_HOME}/WEB-INF/lib/jdom.jar:\
${ROLLER_HOME}/WEB-INF/lib/jrcs-diff.jar:\
${ROLLER_HOME}/WEB-INF/lib/jta.jar:\
${ROLLER_HOME}/WEB-INF/lib/jython.jar:\
${ROLLER_HOME}/WEB-INF/lib/log4j-1.2.11.jar:\
${ROLLER_HOME}/WEB-INF/lib/lucene-1.4.3.jar:\
${ROLLER_HOME}/WEB-INF/lib/oscache.jar:\
${ROLLER_HOME}/WEB-INF/lib/roller-business.jar:\
${ROLLER_HOME}/WEB-INF/lib/roller-web.jar:\
${ROLLER_HOME}/WEB-INF/lib/rome-0.8.jar:\
${ROLLER_HOME}/WEB-INF/lib/rome-fetcher-0.8.jar:\
${ROLLER_HOME}/WEB-INF/lib/saxpath.jar:\
${ROLLER_HOME}/WEB-INF/lib/textile4j-1.20.jar:\
${ROLLER_HOME}/WEB-INF/lib/velocity-1.4.jar:\
${ROLLER_HOME}/WEB-INF/lib/xmlrpc-1.2-b1.jar:\
${CATALINA_HOME}/server/lib/servlet-api.jar:\
${_JDBCJAR}

${JAVA_HOME}/bin/java -Dcatalina.home=${CATALINA_HOME} -Droller.custom.config=${ROLLER_CONFIG} -classpath ${_CP} $1

