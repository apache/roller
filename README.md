Welcome to TightBlog! This project started off in May 2015 as a fork of the Apache Roller project.  As of 17 July 2016, <a href="https://github.com/gmazza/tightblog/releases">Release 1.0.0</a> is available.

TightBlog strives to be the mathematically cleanest and simplest implementation of a Java based blog server, suitable either for direct use or
incorporation, as an Apache-licensed open source project, into larger projects.  Specifically, its goal is to satisfy all the needs of 80% of bloggers while
avoiding seldom-requested functionality that bloats the application and creates maintenance burdens, doing more harm than good.

This more realistic goal--along with adopting the Spring framework, REST, and other code modernizations--has allowed TightBlog to slim down considerably from its parent:
The 1.0.0 release of TightBlog uses 17 database tables compared to Roller V5.1.2's 33, 187 Java source files to 493 in Roller, and 55 JSPs vs. Roller's 96.  Only increase,
a nice one, is about 15 more JavaScript files have been added, due to TightBlog's increased emphasis on browser-side processing.

TightBlog 2.0 is underway.  Simplifications continuing, as of 29 April 2017 the application is at 14 database tables, 156 Java source files and 37 JSPs.

Check <a href="https://web-gmazza.rhcloud.com/blog/category/Blogs+%26+Wikis">my blog</a> for recent status updates.

The top-level TightBlog directory consists of the following folders:

* app:                    TightBlog application - WAR application meant for deployment on a servlet container
* it-selenium:            Integrated browser tests for TightBlog using Selenium
* etc:                    Utility scripts and screenshots for documentation

To obtain the source code:
* latest (2.0 branch):  git clone git@github.com:gmazza/tightblog.git
* 1.0 branch: https://github.com/gmazza/tightblog/tree/tb10branch
* source for a specific release: https://github.com/gmazza/tightblog/releases

To build the application (app/target/tightblog.war) with Maven and Java 8:
  `mvn clean install` from the TightBlog root.

It's *very* quick and simple to try out TightBlog locally, to determine if this is a product you would like to blog with
before proceeding with an actual install.  After building the distribution via `mvn clean install`, navigate to the `app` folder and run `mvn jetty:run`,
and view http://localhost:8080/tightblog from a browser.  From there you can register an account, create a sample blog and some entries,
create and view comments, modify templates, etc., etc., everything you can do with production TightBlog.  Each time it is run,
"mvn jetty:run" creates a new in-memory temporary database that exists until you Ctrl-Z out of the terminal window running this command.

For actual installations on Tomcat or other servlet container, please read the <a href="https://github.com/gmazza/tightblog/wiki">Install pages</a>
on the TightBlog Wiki.
