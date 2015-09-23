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
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.business;

import org.apache.roller.weblogger.WebloggerException;

/**
 * Thread management for executing scheduled and asynchronous tasks.
 */
public interface ThreadManager {
    
    /**
     * Initialize the thread management system.
     *
     * @throws WebloggerException If there is a problem during initialization.
     */
    void initialize() throws WebloggerException;
    
    
    /**
     * Execute runnable in background (asynchronously).
     * @param runnable runnable to run
     * @throws java.lang.InterruptedException
     */
    void executeInBackground(Runnable runnable)
        throws InterruptedException;
    
    
    /**
     * Execute runnable in foreground (synchronously).
     */
    void executeInForeground(Runnable runnable)
        throws InterruptedException;
    
    /**
     * Shutdown.
     */
    void shutdown();
    
    
    /**
     * Release all resources associated with Roller session.
     */
    void release();
    
}
