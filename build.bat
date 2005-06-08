@echo off

REM is this really necessary?
if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point to your Java SDK install directory
goto cleanup
:gotJavaHome 

REM set rocp=%rocp%;.\tools\lib\xercesImpl-2.3.0.jar
REM set rocp=%rocp%;.\tools\lib\xmlParserAPIs-2.3.0.jar
REM set rocp=%rocp%;.\tools\hibernate-2.0\lib\xalan.jar

set rocp=%JAVA_HOME%\lib\tools.jar

set rocp=%rocp%;.\tools\buildtime\ant-1.5.1.jar
set rocp=%rocp%;.\tools\buildtime\ant-optional-1.5.1.jar
set rocp=%rocp%;.\tools\buildtime\NetComponents1.3.8a.jar
set rocp=%rocp%;.\tools\buildtime\junit-3.8.1.jar

echo %rocp%
java -classpath %rocp% org.apache.tools.ant.Main %1 %2 %3 %4 %5



