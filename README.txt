README.txt

This file exists at the top-level of the Roller source tree.

Roller is made up of the following Maven projects:

  roller-project:     Top level project
  roller-core:        Core Roller component
  planet-business:    Planet POJOs and business logic
  planet-web:         Planet webapp (under construction as before)
  weblogger-business: Weblogger POJOs and business logic
  weblogger-web:      The Roller Weblogger Web Classes, Servlets, Filters, etc.
  weblogger-webapp:   The Roller Weblogger webapp, JSP pages, Velocity templates
  weblogger-assembly: Assembly that builds Roller distro
  test-utils:         Test utils (e.g. start/stop Derby task)

To pull the latest trunk sources you do this.

  svn co https://svn.apache.org/repos/asf/roller/trunk
  cd trunk

Building this version of Roller requires Apache Maven 3 to build.


---------------------------
BUILDING FOR TOMCAT

The normal Roller build creates a product suitable for use on Tomcat 6 or later,
which includes OpenJPA buildcode enhancement, OpenJPA and other Java EE jars.

After pulling the source tree and changing directory to its top level, as
indicated above, the following command will build and run all unit tests:

   mvn clean
   mvn install

After doing that, you should find the newly built Roller webapp, suitable
for use with Tomcat in weblogger-web/target/roller. 

To build Roller release files, you do this:

   cd weblogger-assembly
   mvn install
   
After that, you'll find Roller distribution files in weblogger-assembly/target. 
The Tomcat specific release files will have 'for-tomcat' in their names.


---------------------------
BUILDING FOR JAVA EE 6

The Tomcat build includes extra things that are not needed on a full Java EE
application server. In fact, the Tomcat won't work on some Java EE servers.
If you leave those extra things out, Roller can run on most Java EE servers.

If you add a 'javaee' flag to the Roller build invocation, you can create 
Roller release files that will work on a Java EE 6 app server.

    mvn clean
    mvn -Djavaee=true install

    cd weblogger-war-assembly
    mvn -Djavaee=true install

    cd ../weblogger-assembly
    mvn -Djavaee=true install

When that finishes, you will find Roller distribution files in 
weblogger-assembly/target. The Java EE specific release files will have 
'for-javaee' in their names.
      

---------------------------
BUILDING FOR JBOSS 6

JBoss 6 is a Java EE server, but due to differences in JNDI naming, it needs
a separate build.

If you add a 'jboss' flag to the Roller build invocation, you can create 
Roller release files that will work on a JBoss 6 app server.

    mvn clean
    mvn -Djboss=true install

    cd weblogger-war-assembly
    mvn -Djboss=true install

    cd ../weblogger-assembly
    mvn -Djboss=true install

When that finishes, you will find Roller distribution files in 
weblogger-assembly/target. The Java EE specific release files will have 
'for-jboss' in their names.
      

NOTE: If you wish to pull a branch other than the trunk, replace the word
"trunk" in both lines above with the appropriate branch name.  Note that
versions of Roller before 5.0 have an ant-based build.  In general, you should
be able to follow instructions accompanying the sources that you pull in order
to build that version.
