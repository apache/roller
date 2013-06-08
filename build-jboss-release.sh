# How to build the JBoss release of Roller
# See build-tomcat-release.sh for differences between release builds
mvn clean
mvn -Djavaee=true -Djboss=true install

cd weblogger-war-assembly
mvn clean
mvn -Djboss=true install
cd ..

cd weblogger-assembly
mvn clean
mvn -Djboss=true install
cd ..
