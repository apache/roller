# How to build the Java EE release of Roller (Glassfish)
# Builds differ primarily by JARs bundled in the WAR, 
# see weblogger-war-assembly/src/main/assembly/*.xml for JAR diffs
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
