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
JAVA_HOME=/usr/local/jdk1.5
export JAVA_HOME

# Directory of Roller context
WEBAPP_DIR=/usr/local/tomcat/webapps/roller
export WEBAPP_DIR

# Directory of additional jars
JARS_DIR=/usr/local/tomcat/common/lib
export JARS_DIR

# Planet configuration override file specifying JDBC connection parameters
CONFIG_OVERRIDE=roller-custom.properties
export CONFIG_OVERRIDE


# --- YOU SHOULD NOT NEED TO EDIT BELOW THIS LINE ---

${JAVA_HOME}/bin/java \
    -Droller.custom.config=${CONFIG_OVERRIDE} \
    -cp ${WEBAPP_DIR}/WEB-INF/lib/roller-business.jar \
    ${WEBAPP_DIR}/WEB-INF/lib/roller-core.jar \
    org.apache.roller.business.runnable.TaskRunner \
    ${WEBAPP_DIR} ${JARS_DIR} \
    $1

