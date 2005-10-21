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
package org.roller.util.rome;

import java.util.Date;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.impl.ConverterForRSS20;

/**
 * Workaround Rome bug.
 */
public class PlanetConverterForRSS20 extends ConverterForRSS20 {

    public PlanetConverterForRSS20() {
        this("rss_2.0");
    }
    protected PlanetConverterForRSS20(String type) {
        super(type);
    }
    protected SyndEntry createSyndEntry(Item item) {
        DCModule dcm = (DCModule)item.getModule(DCModule.URI);
        Date dcdate = dcm != null ? dcm.getDate() : null;
        SyndEntry syndEntry = super.createSyndEntry(item);
        if (dcdate != null)
        {
            ((DCModule)syndEntry.getModule(DCModule.URI)).setDate(dcdate);
        }
        return syndEntry;
    }
}
