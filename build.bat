@echo off

REM is this really necessary?
if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point to your Java SDK install directory
goto cleanup
:gotJavaHome 

set rocp=%JAVA_HOME%\lib\tools.jar

set rocp=%rocp%;.\tools\buildtime\ant-1.6.2\ant.jar
set rocp=%rocp%;.\tools\buildtime\ant-1.6.2\ant-launcher.jar
set rocp=%rocp%;.\tools\buildtime\ant-1.6.2\ant-junit.jar
set rocp=%rocp%;.\tools\buildtime\ant-1.6.2\ant-commons-net.jar
set rocp=%rocp%;.\tools\buildtime\ant-1.6.2\commons-net-1.2.2.jar
set rocp=%rocp%;.\tools\buildtime\ant-1.6.2\jakarta-oro-2.0.8.jar

set rocp=%rocp%;.\tools\buildtime\junit-3.8.1.jar

echo %rocp%
java -classpath "%rocp%" org.apache.tools.ant.Main %1 %2 %3 %4 %5



