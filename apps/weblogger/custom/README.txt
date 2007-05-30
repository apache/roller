
README.txt for roller/custom directory

Define filesets, paths, and src elements in the files to add code to the Roller build:

   custom-src-forms.xmlf - (fileset) classes to be included in Struts form generation
   custom-src-pojos.xmlf - (fileset)classes to be included in Hibernate mapping generation
   custom-src-beans.xmlf - (src) classes to be included in rollerbeans.jar compilation
   custom-src-web.xmlf -   (src) classes to be included in rollerweb.jar compilation
   custom-web.xmlf -       (fileset) file to be copied into Roller's web context
   custom-dbscripts.xmlf - (path) SQL files to be added in
   custom-jars.xmlf -      (fileset) jars to add to base classpath and to build

Define tasks be be executed during build process:

   custom-post-dbtest.xmlf - (tasks) to be run after DB startup in test-hibernate
   custom-pre-dbtest.xmlf -  (tasks) to be run just before DB shutdown in test-hibernate


