#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  The ASF licenses this file to You
# under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.  For additional information regarding
# copyright in this work, please see the NOTICE file in the top level
# directory of this distribution.

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