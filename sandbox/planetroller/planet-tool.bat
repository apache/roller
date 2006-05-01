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
call setenv.bat
set _CP=.;%PLANETCLASSES%
set _CP=%_CP%;%FETCHERJARS%\commons-httpclient-2.0.2.jar
set _CP=%_CP%;%FETCHERJARS%\rome-fetcher-0.6.jar
set _CP=%_CP%;%ROLLERJARS%\rollerbeans.jar
set _CP=%_CP%;%ROLLERJARS%\commons-logging.jar
set _CP=%_CP%;%ROLLERJARS%\commons-lang-2.0.jar
set _CP=%_CP%;%ROLLERJARS%\jaxen-full.jar
set _CP=%_CP%;%ROLLERJARS%\jdom.jar
set _CP=%_CP%;%ROLLERJARS%\dom4j-1.4.jar
set _CP=%_CP%;%ROLLERJARS%\rome-0.6.jar
set _CP=%_CP%;%ROLLERJARS%\velocity-1.4.jar
set _CP=%_CP%;%ROLLERJARS%\velocity-dep-1.4.jar
java -classpath %_CP% org.roller.tools.planet.PlanetTool %1 