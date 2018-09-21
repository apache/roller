Welcome to TightBlog, a greatly modernized & streamlined bottom-to-(nearly)-top rewrite of Apache Roller, with much
out-of-date and seldom used functionality removed and much new functionality added in as well.  I started this fork in 
May 2015 after contributing for about 2 1/2 years on Roller.  As of 21 September 2018, 
<a href="https://github.com/gmazza/tightblog/releases">Release 3.3.2</a> is available.

Screen shots for the TightBlog UI are [here](https://github.com/gmazza/tightblog/wiki/Screenshots), the twelve-table database model is
[here](https://github.com/gmazza/tightblog/blob/master/app/src/main/resources/dbscripts/createdb.vm), see also [my blog](https://glenmazza.net/blog/) for an example
of a TightBlog-powered blog.  TightBlog ships with three blog themes, for which bloggers may use directly, modify them per-blog, or create their own themes from scratch.

The table below shows the streamlining TightBlog has gone through in its first three releases since forking.  Switching to Gradle,
Spring Boot, Spring REST API, AngularJS and other code modernizations has also made the code much more pleasant to work with
and easier to understand.

|Product|Released|Database Tables|Java Source Files|JSP Files|Lines Of Code|
|-----|-----|-----|-----|-----|-----|
|Apache Roller 5.1.2|1 Mar 2015|33|493|96|95.7K|
|TightBlog 1.0|17 July 2016|17|187|55|48.5K|
|TightBlog 2.0|4 June 2017|14|151|37|43.7K|
|TightBlog 3.0|23 June 2018|12|119|37|35.8K|

(Lines of code--LOC--based on <a href="https://www.openhub.net/p/tightblog">OpenHub</a> stats.  Java source file count does not include unit test classes, however LOC do.)

Some newer functionality in TightBlog not in Roller:

* Bloggers may blog using <a href="http://commonmark.org/">CommonMark</a> in addition to standard HTML and Rich Text Editors.
* Blog entries have a "notes" field for the blogger to store anything helpful in maintaining the article.
* There is a new tag management screen allowing for renaming, merging, and deleting tags attached to blog entries, as well as adding a new tag to all articles already having a given tag.
* Both Category and Tag management tabs now list the number of blog entries associated with the category or tag, as well as publish dates of the first and last entries of each.
* A new "search.enabled" setting has been added to static configuration allowing for shutting off the Lucene indexer used for blog searching, useful in saving processing/space for when you're relying on third party indexing tools like Google Custom Search instead.
* Commenters who request "notify me" to receive emails of future comments for a particular blog entry now receive a link at the bottom of the email to shut off future notifications
* There is a new BLOGCREATOR global role separated from the earlier BLOGGER role.  While both roles allow a blogger full administration of his weblog (whether created by an admin for the blogger or the blogger himself) only users with the former role can create new weblogs.   
* Commenters who are logged-in bloggers now have their blogger ID stored with the comment, simplifying comment entry and allowing for different styling of comments (e.g., different background color for comments made by the blogger on his own blog).
* The blog template engine (used for customized themes) now uses modern Thymeleaf 3.0 instead of Apache Velocity.
* All emails sent are in HTML format and customizable by modifying the Thymeleaf templates in the webapp/thymeleaf/emails folder.
* Login Multifactor Authentication (MFA) with Google Authenticator support added (Admin setting provided to either require it for all bloggers--the default--or have it disabled).

To obtain the source code:
* 3.x branch (current): git clone git@github.com:gmazza/tightblog.git
* source for a specific release: https://github.com/gmazza/tightblog/releases

To build the application (build/libs/tightblog-x.y.z.war) with Gradle and Java 10+:
  `./gradlew clean build` from the TightBlog root.

The Docker images defined in the docker subdirectory of this project can be used to test TightBlog locally before deploying.  First build
the project to generate the tightblog WAR.  As TightBlog requires SSL, next provide a certificate & key for the Tomcat
image as explained in the web Dockerfile.  Then from the docker folder, running "docker-compose up" should result in a TightBlog available
from your local machine at https://localhost/tightblog.  Should you need to change the docker-compose.yml or the web Dockerfile, be sure
to run "docker-compose build" for the images to be regenerated.  Note for simplicity the default does not demo Google Authenticator MFA,
if desired modify the docker/web/tightblog-custom.properties to activate. Emailing is also not configured.

Caution: The Docker images have not seen production use and are currently meant for evaluation purposes.  Presumably more 
tightening of the Tomcat and PostgresQL images would be needed for production use, best to check online sources 
for securing these products should you choose to use these images for your blog.  For installations 
on standalone Tomcat or other servlet container, please read the <a href="https://github.com/gmazza/tightblog/wiki">Install pages</a>
on the TightBlog Wiki.
