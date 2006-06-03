@echo off
call setenv.bat
set _CP=.;%PLANETCLASSES%
set _CP=%_CP%;%FETCHERJARS%\commons-httpclient-2.0.2.jar
set _CP=%_CP%;%FETCHERJARS%\rome-fetcher-0.6.jar
set _CP=%_CP%;%ROLLERJARS%\rollerbeans.jar
set _CP=%_CP%;%ROLLERJARS%\commons-logging.jar
set _CP=%_CP%;%ROLLERJARS%\commons-lang-2.0.jar
set _CP=%_CP%;%ROLLERJARS%\jaxen-full.jar
set _CP=%_CP%;%ROLLERJARS%\jdom.jar
set _CP=%_CP%;%ROLLERJARS%\dom4j-1.4.jar
set _CP=%_CP%;%ROLLERJARS%\rome-0.6.jar
set _CP=%_CP%;%ROLLERJARS%\velocity-1.4.jar
set _CP=%_CP%;%ROLLERJARS%\velocity-dep-1.4.jar
java -classpath %_CP% org.roller.tools.planet.PlanetTool %1 