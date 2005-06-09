#!/bin/bash
source ./setenv.sh
_CP=.:${PLANETCLASSES}
_CP=${_CP}:${FETCHERJARS}/commons-httpclient-2.0.2.jar
_CP=${_CP}:${FETCHERJARS}/rome-fetcher-0.6.jar
_CP=${_CP}:${ROLLERJARS}/rollerbeans.jar
_CP=${_CP}:${ROLLERJARS}/commons-logging.jar
_CP=${_CP}:${ROLLERJARS}/commons-lang-2.0.jar
_CP=${_CP}:${ROLLERJARS}/jaxen-full.jar
_CP=${_CP}:${ROLLERJARS}/jdom.jar
_CP=${_CP}:${ROLLERJARS}/dom4j-1.4.jar
_CP=${_CP}:${ROLLERJARS}/rome-0.6.jar
_CP=${_CP}:${ROLLERJARS}/velocity-1.4.jar
_CP=${_CP}:${ROLLERJARS}/velocity-dep-1.4.jar
java -classpath ${_CP} org.roller.tools.planet.PlanetTool $1 