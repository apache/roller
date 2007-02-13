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
package org.apache.roller.util;
        
import java.io.File;
import org.apache.roller.util.StandaloneWebappClassLoader;

/**
 * Sets up classpath for Roller and runs a task. 
 * Expects these JVM parameters:
 * webapp.dir must specify Roller webapp directory
 * jars.dir must specify additional jars directory (e.g. Tomcat commons/lib)
 */
public class TaskRunner {   
    
    public TaskRunner() {} 
    
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("USAGE: java -cp roller-planet.jar TaskRunner WEBAPPDIR JARSDIR CLASSNAME");
            System.err.println("WEBAPPDIR: The directory path to the web application ");
            System.err.println("           (e.g. $CATALINA_HOME/webapps/roller)");
            System.err.println("JARSDIR:   The directory path to the additional jars ");
            System.err.println("           directory (e.g. $CATALINA_HOME/common/lib)");
            System.err.println("CLASSNAME: The name of the class to be executed by TaskRunner ");
            System.exit(-1);
        }
        String webappDir     = args[0];
        String jarsDir       = args[1];
        String taskClassName = args[2];
        System.out.println("WEBAPPDIR = " + webappDir); 
        System.out.println("JARSDIR   = " + jarsDir);
        System.out.println("CLASSNAME = " + taskClassName);
        
        File webappDirFile = new File(webappDir);
        File jarsDirFile = new File(jarsDir);
        if (!webappDirFile.isDirectory() || !jarsDirFile.isDirectory()) {
            System.err.println("ERROR: webapp.dir and jars.dir must specify existing directories");
            System.exit(-1);
        }        
        
        ClassLoader cl = new StandaloneWebappClassLoader(webappDir, jarsDir, null);
       
        // We're using the new classloader from here on out
        Thread.currentThread().setContextClassLoader(cl);

        // Go!
        Class taskClass = cl.loadClass(taskClassName);
        Runnable task = (Runnable)taskClass.newInstance();
        task.run();
    }
}


/* for example:
 
java \
    -Dplanet.custom.config=planet-custom.properties \
    -Dcatalina.base=. \
    -cp ./build/webapp/WEB-INF/lib/roller-business.jar \
    org.apache.roller.util.TaskRunner \
    ~/roller_trunk/sandbox/planetroller/build/webapp \
    /Applications/Java/jakarta-tomcat-5.5.9/common/lib \
    org.apache.roller.planet.tasks.GeneratePlanetTask
 
 */
