
package org.roller.tomcat;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.roller.util.HSQLDBUtility;

public class TomcatHSQLDBPlugin implements LifecycleListener {
	
	public void lifecycleEvent(LifecycleEvent event) {	
		
		if (event.getType().equals(Lifecycle.START_EVENT)) {
			HSQLDBUtility.start();
		}
		else if (event.getType().equals(Lifecycle.STOP_EVENT)) {
			HSQLDBUtility.stop();
            
            // This is drastic, but 
            // 1) we really want Tomcat to stop and 
            // 2) this is a DEMO bundle
            System.exit(0);
		}
		else {
			System.out.println(getClass().getName() 
               + ": Not handling LifecycleEvent: " + event.getType());
		}
	}
}
