/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
package org.apache.roller.business.runnable;
        
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Sets up classpath for Roller and runs a task. 
 * Expects these JVM parameters:
 * webapp.dir must specify Roller webapp directory
 * jars.dir must specify additional jars directory (e.g. Tomcat commons/lib)
 */

public class TaskRunner {
    
    public static String WEBAPP_DIR = "webapp.dir"; 
    public static String JARS_DIR = "jars.dir"; 
    public static String FS = File.separator;
    
    public TaskRunner() {} 
    
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("USAGE: java -Dwebapp.dir=WEBAPPDIR -Djars.dir=JARSDIR -cp roller-planet.jar TaskRunner CLASSNAME");
            System.err.println("WEBAPPDIR: The directory path to the web application ");
            System.err.println("           (e.g. $CATALINA_HOME/webapps/roller)");
            System.err.println("JARSDIR:   The directory path to the additional jars ");
            System.err.println("           directory (e.g. $CATALINA_HOME/common/lib)");
            System.err.println("CLASSNAME: The name of the class to be executed by TaskRunner ");
            System.exit(-1);
        }
        String taskClassName = args[0];
        String webappDir = System.getProperties().getProperty(WEBAPP_DIR);
        String jarsDir = System.getProperties().getProperty(JARS_DIR);
        if (webappDir == null || jarsDir == null) {
            System.err.println("ERROR: system properties webapp.dir and jars.dir not found");
            System.exit(-1);
        }
        
        File webappDirFile = new File(webappDir);
        File jarsDirFile = new File(jarsDir);
        if (!webappDirFile.isDirectory() || !jarsDirFile.isDirectory()) {
            System.err.println("ERROR: webapp.dir and jars.dir must specify existing directories");
            System.exit(-1);
        }        
        
        // Create collection of URLs needed for classloader
        List urlList = new ArrayList();

        // Add WEB-INF/lib jars
        String libPath = webappDir + FS + "WEB-INF" + FS + "lib";
        addURLs(libPath, urlList);
        
        // Added WEB-INF/classes
        String classesPath = webappDir + FS + "WEB-INF" + FS + "classes" + FS;
        urlList.add(new URL("file://" + classesPath));
        
        // Add additional jars
        addURLs(jarsDir, urlList);
        
        // Create classloader and make it load the task class to be run
        URLClassLoader cl = URLClassLoader.newInstance(
            (URL[])urlList.toArray(new URL[urlList.size()]), null);
        Class taskClass = cl.loadClass(taskClassName);
        Runnable task = (Runnable)taskClass.newInstance();
        
        // We're using the new classloader from here on out
        Thread.currentThread().setContextClassLoader(cl);
        
        // Go!
        task.run();
    }
    
    private static void addURLs(String dirPath, List urlList) throws Exception {
        File libDir = new File(dirPath);
        String[] libJarNames = libDir.list(new FilenameFilter() {
            public boolean accept(File dir, String pathname) {
                if (pathname.endsWith(".jar")) {
                    return true;
                }
                return false;
            }
        });       
        for (int i=0; i<libJarNames.length; i++) {
            String url = "file://" + dirPath + FS + libJarNames[i];
            urlList.add(new URL(url));
        }
    }
}

/* for example:
 java \
    -Dwebapp.dir=/export/home/dave/roller_trunk/sandbox/planetroller/build/webapp \
    -Djars.dir=/export/home/dave/tomcat/common/lib \
    -Dplanet.custom.config=planet-custom.properties \
    -Dcatalina.base=. \
    -cp ./build/webapp/WEB-INF/lib/roller-business.jar \
    org.apache.roller.business.runnable.TaskRunner \
    org.apache.roller.planet.tasks.GeneratePlanetTask
 */
