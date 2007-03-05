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

import java.io.PrintWriter;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.derby.drda.NetworkServerControl;

public class TomcatDerbyPlugin implements LifecycleListener {
	
	public void lifecycleEvent(LifecycleEvent event) {	
		
		if (event.getType().equals(Lifecycle.START_EVENT)) {
            Thread server = new Thread() {
                public void run() {
                    try {
                        System.out.println("Starting Derby");
                        NetworkServerControl server = new NetworkServerControl();
                        server.start(new PrintWriter(System.out));
                    } catch (Exception e) {
                        System.out.println("ERROR staring up Derby");
                        e.printStackTrace();
                    }
                }
            };
            server.start();
		}
		else if (event.getType().equals(Lifecycle.STOP_EVENT)) {
            try {
                System.out.println("Stopping Derby");
                NetworkServerControl server = new NetworkServerControl();
                server.shutdown();
                try {Thread.sleep(2000);} catch (Exception ignored) {}
            } catch (Exception e) {
                System.out.println("ERROR shutting down Derby");
                e.printStackTrace();
            }
            
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
