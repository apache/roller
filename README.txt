README.txt

Welcome to TightBlog! Starting off in May 2015 as a fork of the Apache Roller project (Before then, I was a Roller committer for 2 1/2 years before deciding to fork due to accumulating differences of opinion), TightBlog strives to be the mathematically cleanest and simplest implementation of a Java based blog and planet server, suitable either for direct use or incorporation, as an Apache-licensed open source project, into larger projects.

TightBlog has as a secondary goal to serve as an increasingly helpful sample application for the components it uses (such as the Spring Framework, EclipseLink, and Velocity), with code that can be easily leveraged by others wanting to know how to do X and Y and Z with these tools.  To that end, I'm hoping to receive feedback by fellow developers in all areas where it falls short in that regard.

As of 20 May 2016, TightBlog uses 18 database tables compared to Roller V5.1.2's 33, and has slimmed down to 214 Java source files from the 493 in Roller.

TightBlog is unfortunately not yet ready for release, the Milestone 1.0-tagged issues must all be complete first.

Check my blog at https://web-gmazza.rhcloud.com/blog/category/Blogs+%26+Wikis for recent status updates.

The top-level TightBlog directory consists of the following folders:

  app:                    Weblogger webapp, JSP pages, Velocity templates
  docs:                   Documentation in ODT (OpenOffice/LibreOffice) format
  it-selenium             Integrated browser tests for TightBlog using Selenium
  util:                   Utility scripts (during development and/or application use)

To obtain the source code:
  git clone git@github.com:gmazza/tightblog.git

To build the application (app/target/tightblog.war) with Maven:
  mvn clean install from the TightBlog root.  (Java 8 required).

To deploy the application, (mostly) follow the instructions in the Roller install guide
  http://svn.apache.org/viewvc/roller/trunk/docs/
