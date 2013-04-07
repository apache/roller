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
package org.apache.roller.planet.util.rome;

import org.jdom.Element;

import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.impl.DateParser;
import com.sun.syndication.io.impl.RSS091UserlandParser;

/**
 */
public class PlanetRSS091UParser extends RSS091UserlandParser {

    public PlanetRSS091UParser() {
        this("rss_0.91U");
    }

    protected PlanetRSS091UParser(String type) {
        super(type);
    }

    protected Item parseItem(Element rssRoot,Element eItem) {
        Item item = super.parseItem(rssRoot, eItem);
        Element e = eItem.getChild("pubDate",getRSSNamespace());
        if (e!=null) {
            item.setPubDate(DateParser.parseRFC822(e.getText()));
        }
        return item;
    }
}
