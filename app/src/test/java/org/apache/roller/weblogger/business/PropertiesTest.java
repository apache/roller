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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Properties related business operations.
 */
public class PropertiesTest  {
    
    public static Log log = LogFactory.getLog(PropertiesTest.class);

    @BeforeEach
    public void setUp() throws Exception {
        // setup weblogger
        TestUtils.setupWeblogger();
    }

    @AfterEach
    public void tearDown() throws Exception {
    }


    @Test
    public void testProperiesCRUD() throws Exception {
        
        // remember, the properties table is initialized during Roller startup
        PropertiesManager mgr = WebloggerFactory.getWeblogger().getPropertiesManager();
        TestUtils.endSession(true);
        
        RuntimeConfigProperty prop = null;
        
        // get a property by name
        prop = mgr.getProperty("site.name");
        assertNotNull(prop);
        
        // update a property
        prop.setValue("testtest");
        mgr.saveProperty(prop);
        TestUtils.endSession(true);
        
        // make sure property was updated
        prop = null;
        prop = mgr.getProperty("site.name");
        assertNotNull(prop);
        assertEquals("testtest", prop.getValue());
        
        // get all properties
        Map<String, RuntimeConfigProperty> props = mgr.getProperties();
        assertNotNull(props);
        assertTrue(props.containsKey("site.name"));
        
        // update multiple properties
        prop = props.get("site.name");
        prop.setValue("foofoo");
        prop = props.get("site.description");
        prop.setValue("blahblah");
        mgr.saveProperties(props);
        TestUtils.endSession(true);
        
        // make sure all properties were updated
        props = mgr.getProperties();
        assertNotNull(props);
        assertEquals("foofoo", props.get("site.name").getValue());
        assertEquals("blahblah", props.get("site.description").getValue());
    }

    @Test
    public void compareAndSetAllowsOnlyOneConcurrentWinner() throws Exception {
        PropertiesManager mgr = WebloggerFactory.getWeblogger().getPropertiesManager();
        RuntimeConfigProperty prop = mgr.getProperty("site.frontpage.weblog.handle");
        String original = prop.getValue();
        prop.setValue("");
        mgr.saveProperty(prop);
        TestUtils.endSession(true);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> first = executor.submit(() -> compareAndSetAfterSignal(
                    "site.frontpage.weblog.handle", "first", ready, start));
            Future<Boolean> second = executor.submit(() -> compareAndSetAfterSignal(
                    "site.frontpage.weblog.handle", "second", ready, start));
            ready.await();
            start.countDown();

            assertNotEquals(first.get(), second.get(), "exactly one update must win");

            RuntimeConfigProperty saved = WebloggerFactory.getWeblogger()
                    .getPropertiesManager().getProperty("site.frontpage.weblog.handle");
            assertTrue("first".equals(saved.getValue()) || "second".equals(saved.getValue()));
            saved.setValue(original);
            WebloggerFactory.getWeblogger().getPropertiesManager().saveProperty(saved);
            TestUtils.endSession(true);
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean compareAndSetAfterSignal(String name, String value,
            CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        start.await();
        try {
            boolean updated = WebloggerFactory.getWeblogger().getPropertiesManager()
                    .compareAndSetProperty(name, "", value);
            WebloggerFactory.getWeblogger().flush();
            return updated;
        } finally {
            WebloggerFactory.getWeblogger().release();
        }
    }
    
}
