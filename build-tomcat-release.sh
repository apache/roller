# How to build a Tomcat release of Roller
mvn clean
mvn install

cd weblogger-war-assembly
mvn clean
mvn -Dtomcat=true install
cd ..

cd weblogger-assembly
mvn clean
mvn -Dtomcat=true install
cd ..

