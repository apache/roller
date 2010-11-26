# How to build a Tomcat release of Roller
mvn clean
mvn install
pushd weblogger-assembly
mvn clean
mvn install
popd

