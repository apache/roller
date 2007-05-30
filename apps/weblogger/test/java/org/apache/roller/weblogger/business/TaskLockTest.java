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

import java.util.Date;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.runnable.RollerTask;
import org.apache.roller.weblogger.business.runnable.RollerTaskWithLeasing;
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
    }
    
    public void tearDown() throws Exception {
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    public void testTaskLockCRUD() throws Exception {
        
        ThreadManager mgr = RollerFactory.getRoller().getThreadManager();
        
        // need a test task to play with
        RollerTask task = new TestTask();
        
        // try to acquire a lock
        assertTrue(mgr.registerLease(task));
        TestUtils.endSession(true);
        
        // make sure task is locked
        assertFalse(mgr.registerLease(task));
        TestUtils.endSession(true);
        
        // try to release a lock
        assertTrue(mgr.unregisterLease(task));
        TestUtils.endSession(true);
    }
    
    
    class TestTask extends RollerTaskWithLeasing {
        
        public String getName() { return "TestTask"; }
        public String getClientId() { return "TestTaskClientId"; }
        public Date getStartTime(Date current) { return current; }
        public int getLeaseTime() { return 300; }
        public int getInterval() { return 1800; }
        public void runTask() { }
        
    }
    
}
