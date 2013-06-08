# How to build the Java EE release of Roller (Glassfish)
# See build-tomcat-release.sh for differences between release builds
mvn clean
mvn -Djavaee=true install

cd assembly-war
mvn clean
mvn -Djavaee=true install
cd ..

cd assembly-release
mvn clean
mvn -Djavaee=true install
cd ..
