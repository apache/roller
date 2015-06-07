README.txt

Welcome to TightBlog, a curated fork of Apache Roller (but no longer Apache Roller and not compatible with it).

Differences between TightBlog and Roller can be seen from the closed issues page:
  https://github.com/gmazza/tightblog/issues?q=is%3Aissue+is%3Aclosed

...and the Roller network graph:
  https://github.com/apache/roller/network

...and the Git compare:
  https://github.com/apache/roller/compare/trunk...gmazza:trunk#files_bucket

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


