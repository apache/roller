
Groovy scripts that run against the Roller API
-------------------------------------------------------------------------------

These scripts are designed to the computer on which Roller is installed. 
Because they use the Roller API directly, you must run them with the provided 
"groovy" script which sets up the classpath correctly for calling into the 
Roller code base. 

Before you can run you must:

1) Have Groovy 1.0 installed on your system

2) Have Roller 4.0 or later installed and configured on yoru system

3) Edit the provided groovy script to set GROOVY_HOME, WEBAPP_DIR and JARS_DIR

4) Edit the provided roller-custom.properties to point to your Roller database

Once you've done that you can run the scripts like so:

   $ ./groovy listusers.gy


TODO: Provide a groovy.bat file so scipts can run on Microsoft Windows
