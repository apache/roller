
Groovy scripts that run against the Roller Admin Protocol
-------------------------------------------------------------------------------

These scripts are designed to against a remote Roller installation using the
Roller Admin Protocol (RAP). 


1) Have Groovy 1.0 installed on your system

2) Put the RAP SDK, JDOM and Commons Codec jars in your classpath. For example:

  $ export CLASSPATH=./lib/jdom.jar:./lib/roller-rap-sdk.jar:./lib/commons-codec-1.3.jar


Once you've done that you can run the scripts like so:

  $ groovy <script> <username> <password> <endpoint-url>

For example:

  $ groovy listusers.gy admin admin http://localhost:8080/roller/roller-services/rap

