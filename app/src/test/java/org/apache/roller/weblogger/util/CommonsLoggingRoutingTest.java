/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Roller logs through the commons-logging API. commons-logging must hand those
 * calls straight to Log4j, and no second implementation of the same API (such
 * as jcl-over-slf4j) may be on the classpath.
 */
public class CommonsLoggingRoutingTest {

    /** Collects the events logged to it. */
    private static final class CollectingAppender extends AbstractAppender {
        private final List<LogEvent> events = new CopyOnWriteArrayList<>();

        CollectingAppender() {
            super("collecting", null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }
    }

    @Test
    public void commonsLoggingUsesTheLog4jApi() {
        assertEquals("org.apache.commons.logging.impl.Log4jApiLogFactory",
                LogFactory.getFactory().getClass().getName());
    }

    @Test
    public void noOtherCommonsLoggingImplementationIsPresent() {
        // jcl-over-slf4j ships its own org.apache.commons.logging classes plus this factory
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("org.apache.commons.logging.impl.SLF4JLogFactory"));
    }

    @Test
    public void commonsLoggingMessagesReachLog4j() {
        String name = CommonsLoggingRoutingTest.class.getName() + ".probe";
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        CollectingAppender appender = new CollectingAppender();
        appender.start();
        LoggerConfig loggerConfig = new LoggerConfig(name, Level.INFO, false);
        loggerConfig.addAppender(appender, Level.INFO, null);
        config.addLogger(name, loggerConfig);
        context.updateLoggers();
        try {
            Log log = LogFactory.getLog(name);
            log.debug("below the level");
            log.info("routed through Log4j");

            assertEquals(1, appender.events.size());
            LogEvent event = appender.events.get(0);
            assertEquals(Level.INFO, event.getLevel());
            assertEquals(name, event.getLoggerName());
            assertTrue(event.getMessage().getFormattedMessage().contains("routed through Log4j"));
        } finally {
            config.removeLogger(name);
            context.updateLoggers();
            appender.stop();
        }
    }
}
