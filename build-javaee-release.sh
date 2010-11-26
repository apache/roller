# How to build the Java EE release of Roller (Glassfish)
mvn clean
mvn -Djavaee=true install
pushd weblogger-assembly
mvn clean
mvn -Djavaee=true install
popd
