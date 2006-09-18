README.txt for planetroller

This sandbox directory is where Roller's planet aggregator was created.
Most of the planet code has moved into Roller, but a command-line tool
called PlanetTool remains here.

Planet Tool

A command-line tool that can be used to generated an aggregated weblog. 
PlanetTool can be run as a scheduled-task, generating static HTML, RSS, 
and OPML files right into a web server directory.

You don't need Roller to use the Planet Tool.

All you need to do is to provide your own Planet Config file 
(e.g. planet-config.xml) and use it on the command-line when you 
run planet-tool.sh.

For more information on the config file and on page templates see this blog entry:
http://www.rollerweblogger.org/page/roller/20050213#rome_texen_planet_roller

For more information on how Planet Tool works:
http://www.rollerweblogger.org/page/roller/20050215#planet_roller_internals

A version of PlanetTool exists as the Chapter 11 example in the RSS and Atom
in Action examples. You can get the code here:
http://blogapps.dev.java.net
