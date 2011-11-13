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

package org.apache.roller.weblogger.ui.rendering.util.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.runnable.Job;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.StaticTemplate;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RendererManager;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository.DeviceType;
import org.apache.roller.weblogger.ui.rendering.model.ModelLoader;
import org.apache.roller.weblogger.ui.rendering.util.WeblogFeedRequest;
import org.apache.roller.weblogger.util.cache.CachedContent;


/**
 * EXPERIMENTAL!!
 *
 * A job which will "warm up" some of the rendering layer caches by iterating
 * over all weblogs in the system and rendering a set of their content to put
 * in the caches for later use.
 *
 * Currently only supports warming up the feed cache.
 */
public class WeblogCacheWarmupJob implements Job {
    
    private static Log log = LogFactory.getLog(WeblogCacheWarmupJob.class);
    
    // inputs from the user
    private Map inputs = null;
    
    
    public void execute() {
        
        log.debug("starting");
        
        // check inputs to see what work we are going to do
        if(inputs != null) {
            
            // what weblogs will we handle?
            List weblogs = (List) inputs.get("weblogs");
            if(weblogs == null) {
                return;
            }
            
            // should we do rss entries feeds?
            if("true".equals((String) inputs.get("feed-entries-rss"))) {
                this.warmupFeedCache(weblogs, "entries", "rss");
            }
            
            // should we do atom entries feeds?
            if("true".equals((String) inputs.get("feed-entries-atom"))) {
                this.warmupFeedCache(weblogs, "entries", "atom");
            }
        }
        
        log.debug("finished");
    }
    
    
    public Map output() {
       return null; 
    }
    
    
    public void input(Map input) {
        this.inputs = input;
    }
    
    
    private void warmupFeedCache(List weblogs, String type, String format) {
        
        if(weblogs == null) {
            return;
        }
        
        // we are working on the feed cache
        WeblogFeedCache feedCache = WeblogFeedCache.getInstance();
        
        long start = System.currentTimeMillis();
        
        Iterator allWeblogs = weblogs.iterator();
        String weblogHandle = null;
        while(allWeblogs.hasNext()) {
            weblogHandle = (String) allWeblogs.next();
            log.debug("doing weblog "+weblogHandle);
            
            try {
                // we need a feed request to represent the data
                WeblogFeedRequest feedRequest = new WeblogFeedRequest();
                feedRequest.setWeblogHandle(weblogHandle);
                feedRequest.setType(type);
                feedRequest.setFormat(format);
                
                
                // populate the rendering model
                HashMap model = new HashMap();
                Map initData = new HashMap();
                initData.put("request", null);
                initData.put("feedRequest", feedRequest);
                initData.put("weblogRequest", feedRequest);
                
                // Load models for feeds
                String feedModels = WebloggerConfig.getProperty("rendering.feedModels");
                ModelLoader.loadModels(feedModels, model, initData, true);
                
                // TODO: re-enable custom models when they are actually used
                // Load weblog custom models
                //ModelLoader.loadCustomModels(weblog, model, initData);
                
                
                // lookup Renderer we are going to use
                Renderer renderer = null;
                Template template = new StaticTemplate(
					"templates/feeds/weblog-"+type+"-"+format+".vm", "velocity");
                renderer = RendererManager.getRenderer(template, DeviceType.standard);
                
                
                // render content.  use default size of about 24K for a standard page
                CachedContent rendererOutput = new CachedContent(24567);
                renderer.render(model, rendererOutput.getCachedWriter());
                
                
                // flush rendered output and close
                rendererOutput.flush();
                rendererOutput.close();
                
                // now just put it in the cache
                String key = feedCache.generateKey(feedRequest);
                feedCache.put(key, rendererOutput);
                
            } catch(Exception e) {
                // bummer, error during rendering
                log.error("Error rendering for weblog "+weblogHandle, e);
            }
        }
        
        long end = System.currentTimeMillis();
        long time = (end-start)*1000;
        
        log.info("Completed warmup for "+type+"/"+format+" in "+time+" secs.");
        
    }
    
}
