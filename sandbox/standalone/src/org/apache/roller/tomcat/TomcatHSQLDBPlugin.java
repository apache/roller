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

package org.apache.roller.tomcat;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.roller.util.HSQLDBUtility;

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
