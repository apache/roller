# Apache Roller

[Apache Roller](http://roller.apache.org) is a Java-based, full-featured,
multi-user and group-blog server suitable for blog sites large and small.
Roller is typically run with Apache Tomcat and MySQL.
Roller is made up of the following Maven projects:

* _roller-project_:         Top level project
* _app_:                    Roller Weblogger webapp, JSP pages, Velocity templates
* _assembly-release_:       Used to create official distributions of Roller
* _docs_:                   Roller documentation in ODT format
* _it-selenium_:            Integrated browser tests for Roller using Selenium

## Documentation

The Roller Install, User and Template Guides are available in ODT format
(for OpenOffice or LibraOffice):

* https://github.com/apache/roller/tree/master/docs

## For more information

Hit the Roller Confluence wiki:

* How to build and run Roller: https://cwiki.apache.org/confluence/x/EM4
* How to contribute to Roller: https://cwiki.apache.org/confluence/x/2hsB
* How to make a release of Roller: https://cwiki.apache.org/confluence/x/gycB
* Other developer resources: https://cwiki.apache.org/confluence/x/D84

## Quick start

Assuming you've got a UNIX shell, Java, Maven and Git:

Get the code:

    $ git clone https://github.com/apache/roller.git

Build and test the code:

    $ cd roller
    $ mvn clean install

Run Roller in Jetty with Derby database:

    $ cd app
    $ mvn jetty:run

Browse to http://localhost:8080/roller






