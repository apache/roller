README for rollertask

rollertask.sh is a script that can be used to run the Roller tasks. Because 
rollertask.sh runs outside of the Roller application, you have to put some
files into place before you can run it. 

Here are the steps I used to setup rollertask.sh to run on my UNIX based system.

1) Create a rollertask directory from which you'll run rollertask.sh
   For example, /usr/local/rollertask

2) Copy the example config files in this directory into the rollertask directory
      hibernate.cfg.xml
      jspwiki.properties
      log4j.properties
      rollerRuntimeConfigDefs.xml
      rollertask.sh
      technorati.license

3) Set the correct paths in rollertask.sh
   Set these environment variables to point to the right places:
      JAVA_HOME=/usr/local/jdk1.5
      CATALINA_HOME=/usr/local/tomcat
      ROLLER_HOME=/usr/local/apache-roller-3.0-incubating/webapp/roller
      ROLLER_CONFIG=${CATALINA_HOME}/common/classes/roller-custom.properties

4) Configure the database connection
   Edit the hibernate.cfg.xml file to set your database connection info

5) For the (optional) Planet RefreshEntriesTask
   If you use the JSPWiki plugin (from roller.dev.java.net), then edit the 
   jspwiki.properties task to set the URL of your wiki.

6) For the (optional) Planet TechnoratiRankingsTask
   You'll need to enter your Technorati API license key in the file 
   technorati.license.

7) Create a cron entry for each task you run.
   For example here are cron entries for running the RefreshEntriesTask
   every 30 minutes and the Technorati rankings task every night.

      30 * * * * (cd /usr/local/rollertask.sh; ./rollertask.sh org.apache.roller.ui.core.tasks.RefreshEntriesTask >> ~/logs/planet.log)

      0 0 * * * (cd /usr/local/rollertask.sh; ./rollertask.sh org.apache.roller.ui.core.tasks.TechnoratiRankings >> ~/logs/rankings.log)


