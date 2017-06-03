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
package org.tightblog.rendering.cache;

import java.time.Clock;

/**
 * A cache entry that expires.
 *
 * We use this class to wrap objects being cached and associate a timestamp
 * and timeout period with them so we can know when they expire.  Negative
 * timestamp values indicate non-expiration.
 */
public class ExpiringCacheEntry {

    private Object value;
    private long timeCached = -1;
    private long timeout = 0;

    public ExpiringCacheEntry(Object value, long timeout) {
        this.value = value;
        this.timeout = timeout;
        this.timeCached = Clock.systemDefaultZone().millis();
    }

    public long getTimeCached() {
        return this.timeCached;
    }

    public long getTimeout() {
        return this.timeout;
    }

    /**
     * Retrieve the value of this cache entry.
     *
     * If the value has expired then we return null.
     */
    public Object getValue() {
        if (this.hasExpired()) {
            return null;
        } else {
            return this.value;
        }
    }

    /**
     * Determine if this cache entry has expired.
     */
    public boolean hasExpired() {
        if (timeout < 0) {
            return false;
        }
        long now = System.currentTimeMillis();
        return ((this.timeCached + this.timeout) < now);
    }
}
