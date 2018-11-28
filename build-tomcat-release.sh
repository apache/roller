# How to build a Tomcat release of Roller
mvn clean
mvn install

pushd weblogger-war-assembly
mvn clean
mvn -Dtomcat=true install
popd

pushd weblogger-assembly
mvn clean
mvn -Dtomcat=true install
popd

