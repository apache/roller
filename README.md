
# Apache Roller

[Apache Roller](http://roller.apache.org) is a Java-based, full-featured, multi-user and group-blog server suitable for blog sites large and small.
Roller is typically run with Apache Tomcat and MySQL.
Roller is made up of the following Maven projects:

* _roller-project_:         Top level project
* _app_:                    Roller Weblogger webapp, JSP pages, Velocity templates
* _assembly-release_:       Used to create official distributions of Roller
* _docs_:                   Roller documentation in ASCII Doc format
* _it-playwright_:          Browser tests for Roller using Playwright (run separately, see below)

## Documentation

The Roller Install, User and Template Guides are available in ODT format (for OpenOffice or LibraOffice):

* <https://github.com/apache/roller/tree/master/docs>

## For more information

Hit the Roller Confluence wiki:

* How to build and run Roller: <https://cwiki.apache.org/confluence/x/EM4>
* How to contribute to Roller: <https://cwiki.apache.org/confluence/x/2hsB>
* How to make a release of Roller: <https://cwiki.apache.org/confluence/x/gycB>
* Other developer resources: <https://cwiki.apache.org/confluence/x/D84>


## Installing Roller 

If you want to run Roller in production, then you should down load the latest official release and install it by following the Installation Guide, which you can find at the documentation link: <https://github.com/apache/roller/tree/master/docs>.


## Quick start: Running via Maven

You probably should not run Roller in production using this technique, but it's a relatively easy way to try Roller for yourself. 
Assuming you've got a UNIX shell, Java, Maven and Git:

Get the code:

    $ git clone https://github.com/apache/roller.git

Compile and build Roller:

    $ cd roller
    $ mvn -DskipTests=true install

Run Roller in Jetty with an in-memory Derby database (for testing only):

    $ mvn jetty:run

Once Jetty is up and running browse to <http://localhost:8080/roller> to try to Roller.


## Quick start: running via Docker

Another way to try Roller is to use Docker. 
This is actually easier than running via Maven because you do not need Maven or Java. 
If you've got Docker, here's how you can run Roller for demo purposes.

Get the code:

    $ git clone https://github.com/apache/roller.git

The compose stack runs Roller against a PostgreSQL database with Keycloak as an OpenID Connect identity provider.
Roller and your browser must both reach Keycloak at the same hostname, so add this line to `/etc/hosts` once:

    127.0.0.1 keycloak

Run Docker Compose to build and launch everything:

    $ cd roller
    $ docker compose up

It will take a while to build and start the Docker image.
Once it's done browse to <http://localhost:8080/> and log in as `admin`/`admin` (administrator) or `user`/`user` (regular user).


## Running the tests

Unit tests run as part of the normal build:

    $ mvn install

Browser-based UI tests live in `it-playwright` and run against a started Roller, whichever way you started it.
See [it-playwright/README.md](it-playwright/README.md) for instructions.
CI runs both on every push and pull request.
