/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

public final class LogCapture extends AbstractAppender implements AutoCloseable {

    private final Logger logger;
    private final Level previousLevel;
    private final List<LogEvent> events = new ArrayList<>();

    public LogCapture(Class<?> source, Level level) {
        super("test-log-capture", null, null, true, Property.EMPTY_ARRAY);
        logger = (Logger) LogManager.getLogger(source);
        previousLevel = logger.getLevel();
        start();
        logger.addAppender(this);
        logger.setLevel(level);
    }

    @Override
    public void append(LogEvent event) {
        events.add(event.toImmutable());
    }

    public List<LogEvent> events() {
        return List.copyOf(events);
    }

    @Override
    public void close() {
        logger.removeAppender(this);
        logger.setLevel(previousLevel);
        stop();
    }
}
