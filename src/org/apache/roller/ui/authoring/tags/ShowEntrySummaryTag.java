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
/* Created on April 14, 2006 */
package org.apache.roller.ui.authoring.tags;

import java.util.HashMap;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;

import org.apache.roller.model.Roller;
import org.apache.roller.model.PluginManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.ui.core.RollerContext;

/**
 * Shows entry summary with plugins applied.
 * @jsp.tag name="ShowEntrySummary"
 */
public class ShowEntrySummaryTag extends TagSupport {
    static final long serialVersionUID = 3166731504235428544L;
    private static Log mLogger =
            LogFactory.getFactory().getInstance(ShowEntrySummaryTag.class);
    
    private String name = null;
    private String property = null;
    private String scope = "request";
    private boolean singleEntry = false;
    
    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        Roller roller = RollerFactory.getRoller();
        WeblogEntryData entry = (WeblogEntryData)
            RequestUtils.lookup(pageContext, name, property, scope);
        if (StringUtils.isNotEmpty(entry.getSummary())) {
            String xformed = entry.getSummary();
            try {        
                if (entry.getPlugins() != null) {
                    RollerContext rctx = 
                        RollerContext.getRollerContext();
                    PluginManager ppmgr = roller.getPagePluginManager();
                    Map plugins = ppmgr.getWeblogEntryPlugins(
                        entry.getWebsite());
                    xformed = ppmgr.applyWeblogEntryPlugins(
                        plugins, entry, entry.getSummary());
                }               
                pageContext.getOut().println(xformed);
            } catch (Throwable e) {
                throw new JspException("ERROR applying plugin to entry", e);
            }
        }
        return TagSupport.SKIP_BODY;
    }
        
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @jsp.attribute required="true"
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return Returns the property.
     */
    public String getProperty() {
        return property;
    }
    /**
     * @jsp.attribute required="false"
     */
    public void setProperty(String property) {
        this.property = property;
    }
    
    /**
     * @jsp.attribute required="false"
     */
    public String getScope() {
        return scope;
    }
    /**
     * @param scope The scope to set.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }
}
