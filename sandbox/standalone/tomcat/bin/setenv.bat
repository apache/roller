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

set JAVA_OPTS=-Dderby.system.home=%CATALINA_HOME%\blogdata -Dderby.drda.portNumber=7475 -DrollerPageDir=%CATALINA_HOME%\wikidata -DrollerStorageDir=%CATALINA_HOME%\wikiattachments

set CATALINA_TMPDIR=\temp