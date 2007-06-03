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
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.ui.rendering.pagers.Pager;
import org.apache.roller.planet.ui.rendering.pagers.PlanetEntriesPager;
import org.apache.roller.planet.ui.rendering.util.PlanetGroupFeedRequest;
import org.apache.roller.planet.ui.rendering.util.PlanetRequest;
import org.apache.roller.planet.util.URLUtilities;


/**
 * Model which provides information needed to render a planet feed.
 */
public class FeedModel extends PlanetGroupModel {
    
    private static Log log = LogFactory.getLog(FeedModel.class);
    
    private PlanetGroupFeedRequest feedRequest = null;
    
    
    /** 
     * Creates an un-initialized new instance, Roller calls init() to complete
     * construction. 
     */
    public FeedModel() {
        super();
    }
    
    
    /** 
     * Template context name to be used for model.
     */
    public String getModelName() {
        return "model";
    }
    
    
    /** 
     * Init page model based on request. 
     */
    public void init(Map initData) throws PlanetException {
        
        // parent gets to go first
        super.init(initData);
        
        // we expect the init data to contain a weblogRequest object
        PlanetRequest planetRequest = (PlanetRequest) initData.get("planetRequest");
        if(planetRequest == null) {
            throw new PlanetException("expected planetRequest from init data");
        }
        
        // PageModel only works on page requests, so cast planetRequest
        // into a PlanetRequest and if it fails then throw exception
        if(planetRequest instanceof PlanetGroupFeedRequest) {
            this.feedRequest = (PlanetGroupFeedRequest) planetRequest;
        } else {
            throw new PlanetException("weblogRequest is not a WeblogPageRequest."+
                    "  PageModel only supports page requests.");
        }
    }
    
    
    public Pager getPager() {
        
        String pagerUrl = URLUtilities.getPlanetGroupURL(getPlanet().getHandle(), feedRequest.getGroupHandle());
        
        return new PlanetEntriesPager(
                null,
                feedRequest.getGroup(),
                pagerUrl,
                0,
                0,
                30);
    }
    
}
