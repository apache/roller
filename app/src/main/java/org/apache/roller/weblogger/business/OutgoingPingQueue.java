/*
   Copyright 2015 Glen Mazza

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.apache.roller.weblogger.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.AutoPing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In memory storage of outgoing pings to be periodically sent out by
 * a continually running task.  Pings are sent to each ping server defined
 * for a blog whenever that blog has a new or updated blog entry.
 */
public class OutgoingPingQueue {
    private static Log log = LogFactory.getLog(OutgoingPingQueue.class);
    private static OutgoingPingQueue instance = null;
    private List<AutoPing> queue = Collections.synchronizedList(new ArrayList<AutoPing>());

    static {
        instance = new OutgoingPingQueue();
    }

    // non-instantiable because we are a singleton
    private OutgoingPingQueue() {
    }

    public static OutgoingPingQueue getInstance() {
        return instance;
    }

    public void addPing(AutoPing ping) {
        for (AutoPing pingTest : queue) {
            if (pingTest.equals(ping)) {
                log.debug("Already in ping queue, skipping: " + ping);
                return;
            }
        }

        this.queue.add(ping);
    }

    public List<AutoPing> getPings() {
        return new ArrayList<>(this.queue);
    }

    public synchronized void clearPings() {
        this.queue = Collections.synchronizedList(new ArrayList<AutoPing>());
    }
}
