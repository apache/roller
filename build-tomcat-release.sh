# How to build a Tomcat release of Roller
# See weblogger-war-assembly/src/main/assembly/*.xml for JAR diffs
# between builds
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

