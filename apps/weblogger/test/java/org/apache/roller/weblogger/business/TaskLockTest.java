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

package org.apache.roller.weblogger.business;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.runnable.RollerTask;
import org.apache.roller.weblogger.business.runnable.ThreadManager;


/**
 * Test TaskLock related business operations.
 */
public class TaskLockTest extends TestCase {
    
    public static Log log = LogFactory.getLog(TaskLockTest.class);
    
    
    public TaskLockTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(TaskLockTest.class);
    }
    
    
    public void setUp() throws Exception {
        // setup weblogger
        TestUtils.setupWeblogger();
    }
    
    public void tearDown() throws Exception {
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     * @throws Exception if one is raised
     */
    public void testTaskLockCRUD() throws Exception {
        
        ThreadManager mgr = WebloggerFactory.getWeblogger().getThreadManager();
        
        // need a test task to play with
        RollerTask task = new TestTask();
        
        // try to acquire a lock
        assertTrue("Failed to acquire lease.",mgr.registerLease(task));
        // We don't flush here because registerLease should flush on its own
        TestUtils.endSession(false);
        
        // make sure task is locked
        assertFalse("Acquired lease a second time when we shouldn't have been able to.",mgr.registerLease(task));
        TestUtils.endSession(false);
        
        // try to release a lock
        assertTrue("Release of lease failed.",mgr.unregisterLease(task));
        // We don't flush here because unregisterLease should flush on its own
        TestUtils.endSession(false);

        // Current unregisterLease semantics are idempotent.  Double release should
        // actually succeed.
        assertTrue("Second release failed.", mgr.unregisterLease(task));
        TestUtils.endSession(false);
    }
    
}
