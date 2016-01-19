README.txt

Welcome to TightBlog, which started off as a fork of Apache Roller in May 2015.  (Prior to starting TightBlog I was a volunteer on the Apache Roller team for 2.5 years.)  Tightblog is not yet ready for release, the Milestone 1.0-tagged Issues must all be complete first.

Check my blog at https://web-gmazza.rhcloud.com/blog/category/Blogs+%26+Wikis for recent status updates.

As of 19 January 2016, Tightblog uses 18 database tables compared to Roller's 33, and has slimmed down to 264 Java source files from the 493 in Roller.

Differences between TightBlog and Roller can be seen from the closed issues page:
  https://github.com/gmazza/tightblog/issues?q=is%3Aissue+is%3Aclosed

...and the Roller network graph:
  https://github.com/apache/roller/network



TightBlog is made up of the following Maven projects:

  roller-project:         Top level project
  app:                    Weblogger webapp, JSP pages, Velocity templates
  docs:                   documentation in ODT (OpenOffice/LibreOffice) format
  it-selenium             Integrated browser tests for TightBlog using Selenium

To obtain the source code:
  git clone git@github.com:gmazza/tightblog.git

To build the application (app/target/roller.war):
  mvn clean install from the TightBlog root.

To deploy the application, follow the instructions in the Roller install guide
  http://svn.apache.org/viewvc/roller/trunk/docs/

