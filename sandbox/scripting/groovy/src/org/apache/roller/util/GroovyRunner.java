/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
 *
 * GroovyRunner.java
 * Created on October 20, 2006, 10:54 PM
 */

package org.apache.roller.util;

import java.io.File;
import groovy.lang.*;
import org.apache.roller.util.*;

/**
 * Setup Roller classloader and run a Groovy script
 */
public class GroovyRunner {
    public GroovyRunner() {} 
    
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("USAGE: java -cp roller-business.jar TaskRunner WEBAPPDIR JARSDIR SCRIPTNAME");
            System.err.println("WEBAPPDIR:  The directory path to the web application ");
            System.err.println("            (e.g. $CATALINA_HOME/webapps/roller)");
            System.err.println("JARSDIR:    The directory path to the additional jars ");
            System.err.println("            directory (e.g. $CATALINA_HOME/common/lib)");
            System.err.println("SCRIPTNAME: The name of the class to be executed by TaskRunner ");
            System.exit(-1);
        }
        String webappDir  = args[0];
        String jarsDir    = args[1];
        String scriptFile = args[2];
        System.out.println("WEBAPPDIR  = " + webappDir); 
        System.out.println("JARSDIR    = " + jarsDir);
        System.out.println("SCRIPTFILE = " + scriptFile);

        File webappDirFile = new File(webappDir);
        File jarsDirFile = new File(jarsDir);
        if (!webappDirFile.isDirectory() || !jarsDirFile.isDirectory()) {
            System.err.println("ERROR: webapp.dir and jars.dir must specify existing directories");
            System.exit(-1);
        }
       
        // Create new classloader and use it from here on out
        ClassLoader cl = new StandaloneWebappClassLoader(
            webappDir, jarsDir, GroovyRunner.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(cl);
        
        // Apparently bug GROOVY-1194 prevents both of these approaches from working
        
        // Approach #1
        //GroovyShell gshell = new GroovyShell(cl);
        //gshell.evaluate(new File(args[2]));
                
        // Approach #2
        //ClassLoader parent = GroovyRunner.class.getClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(cl);
        Class groovyClass = loader.parseClass(new File(scriptFile));
        GroovyObject groovyObject = (GroovyObject)groovyClass.newInstance();
        groovyObject.invokeMethod("run", null);
    }
}


/*
# --- SHOULD NOT NEED TO EDIT BELOW THIS LINE ---

export RGPATH=\
../build/classes:\
${GROOVY_HOME}/embeddable/groovy-all-1.0.jar:\
${GROOVY_HOME}/lib/commons-cli-1.0.jar:\
${WEBAPP_DIR}/WEB-INF/lib/roller-business.jar
echo ${RGPATH}

# Hack: setting catalina.base=. allows us to save log in ./logs
java \
-Droller.custom.config=roller-custom.properties \
-Dcatalina.base=. \
-cp ${RGPATH} org.apache.roller.util.GroovyRunner $WEBAPP_DIR $JARS_DIR $1
*/
