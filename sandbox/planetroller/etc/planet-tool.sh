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

PLANETJARS=./lib
_CP=.
_CP=${_CP}:${PLANETJARS}/roller-planet.jar
_CP=${_CP}:${PLANETJARS}/rome-0.8.jar
_CP=${_CP}:${PLANETJARS}/rome-fetcher-0.8.jar
_CP=${_CP}:${PLANETJARS}/roller-business.jar
_CP=${_CP}:${PLANETJARS}/commons-logging.jar
_CP=${_CP}:${PLANETJARS}/commons-lang-2.0.jar
_CP=${_CP}:${PLANETJARS}/jaxen-full.jar
_CP=${_CP}:${PLANETJARS}/saxpath.jar
_CP=${_CP}:${PLANETJARS}/jdom.jar
_CP=${_CP}:${PLANETJARS}/dom4j-1.6.1.jar
_CP=${_CP}:${PLANETJARS}/velocity-1.4.jar
_CP=${_CP}:${PLANETJARS}/velocity-dep-1.4.jar
java -classpath ${_CP} org.apache.roller.tools.PlanetTool $1 