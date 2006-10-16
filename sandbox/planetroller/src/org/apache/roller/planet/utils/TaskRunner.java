package org.apache.roller.planet.utils;
        
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Sets up classpath for Roller and runs a task. 
 * Requires property file ./taskrunner.properties.
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
            System.err.println(
              "USAGE: java -cp roller-planet.jar TaskRunner <task-class-name>");
            System.exit(-1);
        }
        String taskClassName = args[0];

        Properties props = new Properties();
        try {
            props.load(new FileInputStream("taskrunner.properties"));
        } catch (Exception e) {
            System.err.println("ERROR: opening taskrunner.properties");
            System.exit(-1);
        }
        
        // Create collection of URLs needed for classloader
        List urlList = new ArrayList();

        // Add WEB-INF/lib jars
        String libPath = props.getProperty(WEBAPP_DIR) 
                + FS + "WEB-INF" + FS + "lib";
        addURLs(libPath, urlList);
        
        // Added WEB-INF/classes
        String classesPath = props.getProperty(WEBAPP_DIR) 
                + FS + "WEB-INF" + FS + "classes" + FS;
        urlList.add(new URL("file://" + classesPath));
        
        // Add additional jars
        String jarsPath = props.getProperty(JARS_DIR);
        addURLs(jarsPath, urlList);
        
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
