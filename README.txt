README.txt

This file exists at the top-level of the Roller source tree.

Roller is made up of the following Maven projects:

  roller-project:         Top level project
  app:                    Roller Weblogger webapp, JSP pages, Velocity templates
  assembly-release:       Used to create official distributions of Roller
  docs:                   Roller documentation in ODT (OpenOffice/LibreOffice) format
  it-selenium             Integrated browser tests for Roller using Selenium

To pull the latest trunk sources you need a Subversion client:
  svn co https://svn.apache.org/repos/asf/roller/trunk roller_trunk

Building this version of Roller requires Apache Maven 3.0.5.
  - How to build Roller: https://cwiki.apache.org/confluence/x/EM4
  - To build and run Roller on Eclipse: https://cwiki.apache.org/confluence/x/EM4

----------------------------------------------------------
How to build the source

The normal Roller build creates a product generically suitable for use several
application containers, however see the Roller Install guide for application server
specific configuration information.

After pulling the source tree and changing directory to its top level, as
indicated above, the following command will build and run all unit tests:

   mvn clean install

After doing that, you should find the newly built Roller webapp, suitable
for use in app/target/roller. 

To build Roller, subsequently run "mvn clean install" from the assembly-release
folder.  After that, you'll find Roller distribution files in 
assembly-release/target. 

---------------------------
NOTES

Building other versions of Roller

If you wish to pull a branch other than the trunk, replace the word
"trunk" in both lines above with the appropriate branch name.  Note that
versions of Roller before 5.0 have an Ant-based build.  In general, you should
be able to follow instructions accompanying the sources that you pull in order
to build that version.

