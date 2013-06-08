# How to build a Tomcat release of Roller
# See weblogger-war-assembly/src/main/assembly/*.xml for JAR diffs
# between builds
mvn clean
mvn install

cd assembly-war
mvn clean
mvn -Dtomcat=true install 
cd ..

cd assembly-release
mvn clean
mvn -Dtomcat=true install
cd ..

