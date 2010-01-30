README.txt

This file exists at the top-level of the Roller source tree.

Roller is made up of the following Maven projects:

  roller-project:     Top level project
  roller-core:        Core Roller component
  planet-business:    Planet POJOs and business logic
  planet-web:         Planet webapp (under construction as before)
  weblogger-business: Weblogger POJOs and business logic
  weblogger-web:      The Roller Weblogger webapp, rendering system, Struts2 UI
  weblogger-assembly: Assembly that builds Roller distro
  test-utils:         Test utils (e.g. start/stop Derby task)

To pull the latest trunk sources you do this.

  svn co https://svn.apache.org/repos/asf/roller/trunk
  cd trunk

Building this version of Roller requires Apache Maven 2 to build.  Version 2.0.10 or
higher is suggested.

After pulling the source tree and changing directory to its top level, as indicated above,
the following command will build and run all unit tests:

  mvn install

After doing this, you should find the newly built Roller webapp in weblogger-web/target/roller.

To build a Roller distribution, you do this:

  cd weblogger-assembly
  mvn assembly:single

and you will find Roller distribution files in weblogger-assembly/target


NOTE: If you wish to pull a branch other than the trunk, replace the word "trunk"
in both lines above with the appropriate branch name.  Note that versions of Roller
before 5.0 have an ant-based build.  In general, you should be able to follow
instructions accompanying the sources that you pull in order to build that version.
