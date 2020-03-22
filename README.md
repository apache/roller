TightBlog is a bottom-to-top rewrite of Apache Roller with much lesser-used functionality removed
and new functionality added in (see below for details).  Its goal is to satisfy the needs of 70-80% of bloggers while
serving as a reliable starting point for other developers to fork and add additional customized features.  

After contributing for about 2 1/2 years on Roller, I forked the codebase in May 2015 after I was unable to
get team approval for simplifications I felt vital for the long-term growth of the project.  As of 22 March 2020, 
<a href="https://github.com/gmazza/tightblog/releases">Release 3.7.2</a> is available.

Screen shots for the TightBlog UI are [here](https://github.com/gmazza/tightblog/wiki/Screenshots), the twelve-table database model is
[here](https://github.com/gmazza/tightblog/blob/master/etc/sqlgen/src/main/resources/createdb.vm), see also [my blog](https://glenmazza.net/blog/) for an example
of a TightBlog-powered blog.  TightBlog ships with three blog themes, for which bloggers may use directly, modify them per-blog, or create their own themes from scratch.

The table below shows the streamlining TightBlog has gone through in its first three releases since forking.  Eliminating
little-used functionality while switching to Gradle, Spring Boot, Spring REST API, AngularJS, etc. has made the code
significantly easier to work with and a much more reliable starting point for forking and adding your own enhancements.  

|Product|Released|Database Tables|Java Source Files (non-test)|JSP Files|Lines Of Code (including tests)|
|-----|-----|-----|-----|-----|-----|
|Apache Roller 5.1.2|1 Mar 2015|33|493|96|95.7K|
|TightBlog 1.0|17 July 2016|17|187|55|48.5K|
|TightBlog 2.0|4 June 2017|14|151|37|43.7K|
|TightBlog 3.0|23 June 2018|12|119|37|35.8K|
|TightBlog 3.5|16 June 2019|12|122|38|32.5K|

Lines of code (LOC) above were based on <a href="https://www.openhub.net/p/tightblog">OpenHub</a> stats.  The
droppage would be even more significant were it not for increased test code coverage. 
<a href="https://codetabs.com/">CodeTabs</a> provides a 
<a href="https://codetabs.com/count-loc/count-loc-online.html">real-time comparator</a> allowing one to see how much
more streamlined TightBlog is over its predecessor:  
<a href="https://api.codetabs.com/v1/loc?github=gmazza/tightblog">(TightBlog)</a>
<a href="https://api.codetabs.com/v1/loc?github=apache/roller">(Roller)</a> (allow each link a minute or so to return.)

Some changes and new functionality added to TightBlog post-fork:

* Bloggers may blog using <a href="http://commonmark.org/">CommonMark</a> in addition to pure HTML.
* Blog entries have an unseen "notes" field to store anything helpful in maintaining the article.
* There is a new BLOGCREATOR global role separated from the earlier BLOGGER role.  While both roles allow a blogger full administration of his weblog once created, those with the latter role will need to have their blog(s) created first by an administrator.   
* New Tag management screen for renaming, merging, and deleting tags attached to blog entries, as well as adding a new tag to entries having a given tag.
* Category and Tag management tabs now list the number of blog entries associated with the category or tag, as well as publish dates of the first and last entries of each.
* Commenters who request "notify me" to receive emails of future comments for a particular blog entry now receive a link at the bottom of the email to shut off future notifications
* Commenters who are logged-in have their screen name and email fields already filled in comment forms, also their blogger ID stored with the comment.
* The blog template engine (used for customized themes) is now Thymeleaf instead of Apache Velocity.
* All emails sent are in HTML format and customizable by modifying the Thymeleaf templates that generate them.
* Login Multifactor Authentication (MFA) with smartphone authenticator support added (Admin setting available to disable if desired).
* Much improved database and generated webpage caching and, for the latter, improved cache statistics gathering.
* A "search.enabled" setting has been added to static configuration allowing for shutting off the Lucene indexer used for blog searching, useful in saving processing/space when using third party indexing tools like Google Custom Search instead.
* An <a href="https://github.com/gmazza/tightblog/wiki/Embedding-GitHub-Source-in-blog-entries">endpoint</a> is provided for embedding GitHub code segments within blog entries, allowing you to blog code tutorials without needing to duplicate project code within the article.


To obtain the source code:
* 3.x branch (current): git clone git@github.com:gmazza/tightblog.git
* source for a specific release: https://github.com/gmazza/tightblog/releases

To build the application (build/libs/tightblog-x.y.z.war) with Gradle and Java 11+:
  `./gradlew clean build` from the TightBlog root.
  
Deployment information:

Be sure to first build the application as stated above.  See <a href="https://github.com/gmazza/tightblog/wiki">Install pages</a>
for general installation instructions.
 
1. Deploy as a WAR on standalone Tomcat: covered in above installation instructions.

1. Deploy as a JAR using an in-memory database (non-production, demoing only).  Modify the
resources/application.properties to provide a Java keystore to support TightBlog's SSL (the
file contains instructions for same.)  Then run "gradle bootRun" from a command-line
and TightBlog will be available at https://localhost:8443/tightblog.  Note the in-memory database
is deleted at each app shut-down.

1. Deploy as a Docker container.  Modify the docker/web/application-tbcustom.properties to 
add your keystore configuration and then from the docker folder run "docker-compose build"
followed by "docker-compose up".  The application will be at https://localhost/tightblog.
Note that while tested locally, I have not used the Docker setup in production so it is 
perhaps best considered of Beta quality.
