Welcome to TightBlog! Starting off in May 2015 as a fork of the Apache Roller project, of which I was a member for 2.5 years before deciding to fork due to differing goals and also so I could better realize my vision for the application.  TightBlog strives to be the mathematically cleanest and simplest implementation of a Java based blog and planet server, suitable either for direct use or incorporation, as an Apache-licensed open source project, into larger projects.  Specifically, it is designed to satisfy 100% of the needs of 80% of bloggers--say, accomplish 25 things very well--rather than bloat itself trying to support the 300 things necessary to satisfy 100% of bloggers, risking increased bugs in core functionality, security holes, and ongoing maintenance/upgrade difficulties in the process.

As of 1 July 2016, TightBlog uses 17 database tables compared to Roller V5.1.2's 33, 202 Java source files to 493 in Roller, and 51 JSPs vs. Roller's 92.  However about 20 more JavaScript files have been/will be added, due to TightBlog's increased emphasis on client-side processing.  The OpenHub statistics (https://www.openhub.net/p/tightblog/analyses/latest/languages_summary), updated about once every two weeks, provide trending code size and language breakdown.

Check my blog at https://web-gmazza.rhcloud.com/blog/category/Blogs+%26+Wikis for recent status updates.

The top-level TightBlog directory consists of the following folders:

  app:                    TightBlog application - WAR application meant for deployment on a servlet container
  docs:                   Documentation in ODT (OpenOffice/LibreOffice) format: Install, User, and Templates guides.
  it-selenium:            Integrated browser tests for TightBlog using Selenium
  util:                   Utility scripts (during development and/or application use)

To obtain the source code:
  git clone git@github.com:gmazza/tightblog.git

To build the application (app/target/tightblog.war) with Maven:
  mvn clean install from the TightBlog root.  (Java 8 required).

It's *very* quick and simple to try out TightBlog locally, to quickly determine if this is a product you would like to blog with before proceeding with an actual install.  After building via "mvn clean install", navigate to the "app" folder and run "mvn jetty:run", and view http://localhost:8080/tightblog from a browser.  From there you can register an account, create a sample blog and some entries, create and view comments, modify templates, etc., etc., everything you can do with production TightBlog.  Each time it is run, "mvn jetty:run" creates a new in-memory temporary database that exists until you Ctrl-Z out of the terminal window running this command.

CURRENT STATUS (1 July 2016): Presently only a beta release is available, as I'm doing testing with it.  Also, the Install Guide in the docs folder is for Apache Roller (hasn't been updated yet, although TightBlog installation is approximately the same.)

