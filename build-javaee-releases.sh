mvn clean
mvn -Djavaee=true install
pushd weblogger-assembly
mvn -Djavaee=true install
popd
