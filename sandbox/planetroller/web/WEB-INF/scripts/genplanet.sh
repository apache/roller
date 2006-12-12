#!/bin/bash

# -- ENSURE THESE ARE CORRECT ---

# Directory of Roller context
export WEBAPP_DIR=/export/home/dave/roller_trunk/sandbox/planetroller/build/webapp

# Directory of additional jars
export JARS_DIR=/export/home/dave/tomcat/common/lib

# Planet configuration override file specifying JDBC connection parameters
export CONFIG_OVERRIDE=planet-custom.properties


# --- SHOULD NOT NEED TO EDIT BELOW THIS LINE ---

# Hack: setting catalina.base=. allows us to save log in ./logs

java \
    -Dcatalina.base=. \
    -Dplanet.custom.config=${CONFIG_OVERRIDE} \
    -cp ${WEBAPP_DIR}/WEB-INF/lib/roller-planet.jar \
    org.apache.roller.util.TaskRunner \
    ${WEBAPP_DIR} ${JARS_DIR} \
    org.apache.roller.planet.tasks.GeneratePlanetTask
