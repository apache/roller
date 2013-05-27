# How to build the Java EE release of Roller (Glassfish)
# See build-tomcat-release.sh for differences between release builds
mvn clean
mvn -Djavaee=true install

cd weblogger-war-assembly
mvn clean
mvn -Djavaee=true install
cd ..

cd weblogger-assembly
mvn clean
mvn -Djavaee=true install
cd ..
