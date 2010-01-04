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

To build and run all unit tests, you do this:

  svn co https://svn.apache.org/repos/asf/roller/branches/roller_mavenized
  cd roller_mavenized
  mvn install

You'll find the Roller webapp in weblogger-web/target/roller. To build
a Roller distribution, you do this:

  cd weblogger-assembly
  mvn assembly:single

And you will find Roller distribution files in weblogger-assembly/target

