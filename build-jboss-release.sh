# How to build the JBoss release of Roller
# See build-release.sh for differences between release builds
mvn clean
mvn -Djboss=true install

cd assembly-war
mvn clean
mvn -Djboss=true install
cd ..

cd assembly-release
mvn clean
mvn -Djboss=true install
cd ..
