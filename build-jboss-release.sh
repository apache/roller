# How to build the JBoss release of Roller
mvn clean
mvn -Djavaee=true -Djboss=true install

pushd weblogger-war-assembly
mvn clean
mvn -Djboss=true install
popd

pushd weblogger-assembly
mvn clean
mvn -Djboss=true install
popd
