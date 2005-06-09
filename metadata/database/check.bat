set _cp=..\lib\rollerbeans.jar
set _cp=%_cp%;..\lib\commons-logging.jar
set _cp=%_cp%;%CATALINA_HOME%\common\lib\mysql-connector-java-3.0.14-production-bin.jar
java -classpath %_cp%; org.roller.business.utils.ConsistencyCheck %1 %2
