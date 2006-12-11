/*
 * Copyright 2004 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.roller.util.rome;

import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.impl.ConverterForRSS091Userland;

/**
 */
public class PlanetConverterForRSS091U extends ConverterForRSS091Userland {

    public PlanetConverterForRSS091U() {
        this("rss_0.91U");
    }

    protected PlanetConverterForRSS091U(String type) {
        super(type);
    }
    protected SyndEntry createSyndEntry(Item item) {
        SyndEntry entry = super.createSyndEntry(item);
        entry.setPublishedDate(item.getPubDate()); 
        return entry;
    }
}
