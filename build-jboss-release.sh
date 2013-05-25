# How to build the JBoss release of Roller
# Builds differ primarily by JARs bundled in the WAR, 
# see weblogger-war-assembly/src/main/assembly/*.xml for JAR diffs
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
