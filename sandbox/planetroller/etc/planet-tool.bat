@echo off
rem Licensed to the Apache Software Foundation (ASF) under one or more
rem  contributor license agreements.  The ASF licenses this file to You
rem under the Apache License, Version 2.0 (the "License"); you may not
rem use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.  For additional information regarding
rem copyright in this work, please see the NOTICE file in the top level
rem directory of this distribution.

@echo off
set PLANETJARS=.\lib
set _CP=.
set _CP=%_CP%;%PLANETJARS%\roller-planet.jar
set _CP=%_CP%;%PLANETJARS%\rome-0.8.jar
set _CP=%_CP%;%PLANETJARS%\rome-fetcher-0.8.jar
set _CP=%_CP%;%PLANETJARS%\roller-business.jar
set _CP=%_CP%;%PLANETJARS%\commons-logging.jar
set _CP=%_CP%;%PLANETJARS%\commons-lang-2.0.jar
set _CP=%_CP%;%PLANETJARS%\jaxen-full.jar
set _CP=%_CP%;%PLANETJARS%\saxpath.jar
set _CP=%_CP%;%PLANETJARS%\jdom.jar
set _CP=%_CP%;%PLANETJARS%\dom4j-1.6.1.jar
set _CP=%_CP%;%PLANETJARS%\velocity-1.4.jar
set _CP=%_CP%;%PLANETJARS%\velocity-dep-1.4.jar
java -classpath %_CP% org.apache.roller.tools.PlanetTool %1 