#!/bin/bash
# Executes a Roller program on the command line
#
# Takes one argument, the roller class to run
#

if [ ! "$1" ] ; then
  echo "usage: $0 <classname>"
  exit 0;
fi


# -- YOU MUST ENSURE THESE SETTINGS ARE CORRECT ---

# Path to Java JDK
JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home
export JAVA_HOME

# Directory of Roller context
WEBAPP_DIR=${HOME}/src/roller_4.0/apps/weblogger/build/webapp
export WEBAPP_DIR

# Directory of additional jars (most importantly: your JDBC driver)
JARS_DIR=${HOME}/tomcat/lib
export JARS_DIR

# Roller configuration override file specifying JDBC connection parameters
CONFIG_OVERRIDE=roller-custom.properties
export CONFIG_OVERRIDE

# Planet configuration override file specifying JDBC connection parameters
PLANETCONFIG_OVERRIDE=planet-custom.properties
export PLANETCONFIG_OVERRIDE


# --- YOU SHOULD NOT NEED TO EDIT BELOW THIS LINE ---


ROLLERTASK_CLASSPATH=${WEBAPP_DIR}/WEB-INF/lib/roller-business.jar

# Hack: setting catalina.base=. allows us to save log in ./logs
mkdirs ./logs

${JAVA_HOME}/bin/java \
    -Dcatalina.base=. \
    -Droller.custom.config=${CONFIG_OVERRIDE} \
    -Dplanet.custom.config=${PLANETCONFIG_OVERRIDE} \
    -cp ${ROLLERTASK_CLASSPATH} \
    org.apache.roller.weblogger.business.runnable.TaskRunner \
    ${WEBAPP_DIR} ${JARS_DIR} $1
