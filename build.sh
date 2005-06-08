

if [ "$JAVA_HOME" = "" ] ; then
	echo You must set JAVA_HOME to point to your Java JDK install directory
	exit 1
fi

rocp=${JAVA_HOME}/lib/tools.jar
rocp=${rocp}:./tools/buildtime/xercesImpl-2.3.0.jar
rocp=${rocp}:./tools/buildtime/xmlParserAPIs-2.3.0.jar
rocp=${rocp}:./tools/buildtime/ant-1.5.1.jar
rocp=${rocp}:./tools/buildtime/ant-optional-1.5.1.jar
rocp=${rocp}:./tools/buildtime/NetComponents1.3.8a.jar

java -Djava.home=${JAVA_HOME} -classpath ${rocp} org.apache.tools.ant.Main $1 $2 $3 $4 $5


