/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Queues up incoming hit counts so that they can be recorded to the DB in
 * an asynchronous manner at user-specified intervals.
 */
public final class HitCountQueue {
    private static HitCountQueue instance = null;
    private List<String> queue = Collections.synchronizedList(new ArrayList<>());

    static {
        instance = new HitCountQueue();
    }

    // non-instantiable because we are a singleton
    private HitCountQueue() {
    }

    public static HitCountQueue getInstance() {
        return instance;
    }

    public void processHit(Weblog weblog) {
        // if the weblog isn't null then just drop its handle in the queue
        // each entry in the queue is a weblog handle and indicates a single hit
        if(weblog != null) {
            this.queue.add(weblog.getHandle());
        }
    }

    public List<String> getHits() {
        return new ArrayList<>(this.queue);
    }

    /**
     * Reset the queued hits.
     */
    public synchronized void resetHits() {
        this.queue = Collections.synchronizedList(new ArrayList<>());
    }
}
