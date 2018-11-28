# How to build the Java EE release of Roller (Glassfish)
mvn clean
mvn -Djavaee=true install

pushd weblogger-war-assembly
mvn clean
mvn -Djavaee=true install
popd

pushd weblogger-assembly
mvn clean
mvn -Djavaee=true install
popd
