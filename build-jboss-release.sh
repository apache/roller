# How to build the JBoss release of Roller
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
