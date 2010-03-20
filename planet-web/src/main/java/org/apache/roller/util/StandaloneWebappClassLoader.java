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
 *
 * StandaloneWebappClassLoader.java
 * Created on October 20, 2006, 11:11 PM
 */

package org.apache.roller.util;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassLoader to enable running webapp classes outside of webapp.  
 * You provide webappDir and jarsDir paths and the classloader will include 
 * webappDir/WEB-INF/classes, webappDir/WEB-INF/lib/*jar and jarsDir/*.jar.
 */
public class StandaloneWebappClassLoader extends URLClassLoader {
    public static String FS = File.separator;
    
    /** Use calling class's parent classloader */
    public StandaloneWebappClassLoader(String webappDir, String jarsDir) throws Exception {
        super(buildURLsArray(webappDir, jarsDir));
    }
    
    /** Use a specific parent classloader, or null for no parent */
    public StandaloneWebappClassLoader(String webappDir, String jarsDir, ClassLoader cl) throws Exception {
        super(buildURLsArray(webappDir, jarsDir), cl);
    }
    
    private static URL[] buildURLsArray(String webappDir, String jarsDir) throws Exception {
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
                
        return (URL[])urlList.toArray(new URL[urlList.size()]);  
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
