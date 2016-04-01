README.txt

Welcome to TightBlog, which started off as a fork of Apache Roller in May 2015.  (Prior to starting TightBlog I was a Roller committer for 2.5 years.)  Tightblog is not yet ready for release, the Milestone 1.0-tagged Issues must all be complete first.

Check my blog at https://web-gmazza.rhcloud.com/blog/category/Blogs+%26+Wikis for recent status updates.

As of 1 April 2016, Tightblog uses 18 database tables compared to Roller's 33, and has slimmed down to 245 Java source files from the 493 in Roller.


TightBlog is made up of the following Maven projects:

  tightblog-project:      Top level project
  app:                    Weblogger webapp, JSP pages, Velocity templates
  docs:                   documentation in ODT (OpenOffice/LibreOffice) format
  it-selenium             Integrated browser tests for TightBlog using Selenium

To obtain the source code:
  git clone git@github.com:gmazza/tightblog.git

To build the application (app/target/tightblog.war):
  mvn clean install from the TightBlog root.

To deploy the application, (mostly) follow the instructions in the Roller install guide
  http://svn.apache.org/viewvc/roller/trunk/docs/
