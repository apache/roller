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

package org.apache.roller.planet.ui.rendering.model;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.URLStrategy;
import org.apache.roller.planet.config.PlanetRuntimeConfig;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.ui.rendering.util.PlanetRequest;


/**
 * Provides access to URL building functionality.
 *
 * NOTE: we purposely go against the standard getter/setter bean standard
 * for methods that take arguments so that users get a consistent way to
 * access those methods in their templates. i.e.
 *
 * $url.category("foo")
 *
 * instead of
 *
 * $url.getCategory("foo")
 */
public class PlanetURLModel implements Model {
    
    private static Log log = LogFactory.getLog(PlanetURLModel.class);
    
    private URLStrategy urlStrategy = null;
    private PlanetData planet = null;
    
    
    public PlanetURLModel() {}
    
    public String getModelName() {
        return "url";
    }
    
    public void init(Map initData) throws PlanetException {
        
        // grab a reference to the url strategy
        this.urlStrategy = PlanetFactory.getPlanet().getURLStrategy();
        
        // need a weblog request so that we can know the weblog and locale
        PlanetRequest planetRequest = (PlanetRequest) initData.get("planetRequest");
        if(planetRequest == null) {
            throw new PlanetException("Expected 'planetRequest' init param!");
        }
        
        this.planet = planetRequest.getPlanet();
    }
    
    
    public String getSite() {
        return PlanetRuntimeConfig.getProperty("site.absoluteurl");
    }
    
        
    public String getHome() {
        return urlStrategy.getPlanetURL(planet.getHandle());
    }
    
    
    public String group(String groupHandle) {
        return urlStrategy.getPlanetGroupURL(planet.getHandle(), groupHandle, -1);
    }
    
    
    public String group(String groupHandle, int pageNum) {
        return urlStrategy.getPlanetGroupURL(planet.getHandle(), groupHandle, pageNum);
    }
    
    
    public FeedURLS getFeed() {
        return new FeedURLS();
    }
    
    
    public String opml(String groupHandle) {
        return urlStrategy.getPlanetGroupOpmlURL(planet.getHandle(), groupHandle);
    }
    
    
    ///////  Inner Classes  ///////
    
    public class FeedURLS {
        
        public String rss(String groupHandle) {
            return urlStrategy.getPlanetGroupFeedURL(planet.getHandle(), groupHandle, "rss");
        }
        
        public String atom(String groupHandle) {
            return urlStrategy.getPlanetGroupFeedURL(planet.getHandle(), groupHandle, "atom");
        }
    }
    
}
