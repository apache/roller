README for the Apache Roller site - http://incubator.apache.org/roller

To update the website:

1) Make changes in xdocs directory

2) Run the Ant build.xml script to generate site to docs directory

3) Make sure any new files under ./docs are added to SVN

4) Commit changes in ./xdocs and ./docs to SVN

3) ssh to people.apache.org

4) cd /www/incubator.apache.org/roller

5) svn update

6) Wait up to four hours for changes to be synced to site
