# Full build process for Roller.
mvn clean
mvn install

cd assembly-war
mvn clean
mvn install
cd ..

cd assembly-release
mvn clean
mvn install
cd ..

