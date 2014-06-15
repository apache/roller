README for rollertask

rollertask.sh is a script that can be used to run the Roller tasks. Because 
rollertask.sh runs outside of the Roller application, you have to put some
files into place before you can run it. 

Here are the steps I used to setup rollertask.sh to run on my UNIX based system.

1) Create a rollertask directory from which you'll run rollertask.sh
   For example, /usr/local/rollertask

2) Copy the example config files in this directory into the rollertask directory
      rollertask.sh
      roller-custom.propoerties

3) Edit rollertask.sh to set the correct paths to JDK, Tomcat and extra jars

4) Configure the database connection
   Edit the roller-custom.properties file to set your database connection info

5) Create a cron entry for each task you run.
   For example here is a cron entry for running the RefreshEntriesTask every 30 minutes.

      30 * * * * (cd /usr/local/rollertask.sh; ./rollertask.sh org.apache.roller.ui.core.tasks.RefreshEntriesTask >> ~/logs/planet.log)
