
Groovy scripts that run against the Roller API
-------------------------------------------------------------------------------

The example scripts in the scripts directory are designed to the computer on 
which Roller is installed. Because they use Roller's Java API directly, you 
must run them with the provided "rollergroovy" script which sets up the 
classpath correctly for calling into the Roller code base. 

Before you can run you must:

1) Have Groovy 1.5.4 installed on your system

2) Have Roller 4.0 installed and configured on yoru system

3) Edit the provided rollergroovy script to set the GROOVY_HOME, WEBAPP_DIR and 
   JARS_DIR. If you have a newer version of Groovy than 1.5.4, you'll also have 
   to change some of the jar names to those included in your newer Groovy 
   distrivution. 

4) Edit the provided roller-custom.properties to point to your Roller database
   and to use the correct connection parameters, username, password, etc.

Once you've done that you can run the scripts like so:

   $ ./rollergroovy listusers.gy

For a complete listing of Roller's Java classes see the Roller Javadocs:
    http://people.apache.org/~snoopdave/javadocs/roller40/

TODO: Provide a rollergroovy.bat file so scipts can run on Microsoft Windows
