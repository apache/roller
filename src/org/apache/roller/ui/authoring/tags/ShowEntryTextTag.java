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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;

import org.apache.roller.model.Roller;
import org.apache.roller.model.PluginManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.util.Utilities;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;

/**
 * Shows either entry summary or text, as appropriate and with plugins applied.
 * @jsp.tag name="ShowEntryText"
 */
public class ShowEntryTextTag extends TagSupport {
    static final long serialVersionUID = 3166731504235428544L;
    private static Log mLogger =
            LogFactory.getFactory().getInstance(ShowEntrySummaryTag.class);
    
    private String name = null;
    private String property = null;
    private String scope = "request";
    private boolean noPlugins = false;
    private boolean stripHtml = false;
    private boolean singleEntry = false;
    private int maxLength = -1;
    
    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        Roller roller = RollerFactory.getRoller();
        WeblogEntryData entry = (WeblogEntryData)
            RequestUtils.lookup(pageContext, name, property, scope);  
        
        String sourceText = null;
        boolean hasSummary = Utilities.isNotEmpty(entry.getSummary());
        boolean hasText= Utilities.isNotEmpty(entry.getText());
        if (singleEntry) {
            if (hasText) sourceText = entry.getText();
            else if (hasSummary) sourceText = entry.getSummary();
        } else {
            if (hasSummary) sourceText = entry.getSummary();
            else if (hasText) sourceText = entry.getText();
        }
        if (Utilities.isNotEmpty(sourceText)) {
            try {
                String xformed = sourceText;        
                if (entry.getPlugins() != null) {
                    RollerContext rctx = 
                        RollerContext.getRollerContext();
                    try {
                        PluginManager ppmgr = roller.getPagePluginManager();
                        Map plugins = ppmgr.getWeblogEntryPlugins(
                                entry.getWebsite(),
                                new HashMap());

                        xformed = ppmgr.applyWeblogEntryPlugins(plugins, entry, sourceText);

                    } catch (Exception e) {
                        mLogger.error(e);
                    }
                }

                if (stripHtml) {
                    // don't escape ampersands
                    xformed = Utilities.escapeHTML( Utilities.removeHTML(xformed), false );
                }

                if (maxLength != -1) {
                    xformed = Utilities.truncateNicely(xformed, maxLength, maxLength, "...");
                }

                // somehow things (&#8220) are getting double-escaped
                // but I cannot seem to track it down
                xformed = Utilities.stringReplace(xformed, "&amp#", "&#");

                pageContext.getOut().println(xformed);
                
            } catch (Throwable e) {
                throw new JspException("ERROR applying plugin to entry", e);
            }
        }
        return TagSupport.SKIP_BODY;
    }
    
    /**
     * Maximum length of text displayed, only applies if stripHtml is true.
     * @jsp.attribute required="false"
     * @return Returns the maxLength.
     */
    public int getMaxLength() {
        return maxLength;
    }
    
    /**
     * Maximum length of text displayed, only applies if stripHtml is true.
     * @param maxLength The maxLength to set.
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
    
    /**
     * Set to true to prevent application of plugins.
     * @jsp.attribute required="false"
     * @return Returns the noPlugins.
     */
    public boolean getNoPlugins() {
        return noPlugins;
    }
    
    /**
     * Set to true to prevent application of plugins.
     * @param stripHtml The stripHtml to set.
     */
    public void setNoPlugins(boolean noPlugins) {
        this.noPlugins = noPlugins;
    }
    
    /**
     * Set to true to strip all HTML markup from output.
     * @jsp.attribute required="false"
     * @return Returns the noPlugins.
     */
    public boolean getStripHtml() {
        return stripHtml;
    }
    
    /**
     * Set to true to strip all HTML markup from output.
     * @param stripHtml The stripHtml to set.
     */
    public void setStripHtml(boolean stripHtml) {
        this.stripHtml = stripHtml;
    }
    
    /**
     * Set to true to inform PagePlugins of single entry page.
     * @jsp.attribute required="false"
     */
    public boolean getSingleEntry() {
        return singleEntry;
    }
    
    /**
     * Set to true to inform PagePlugins of single entry page.
     * should "skip" themselves.
     */
    public void setSingleEntry(boolean singleEntry) {
        this.singleEntry = singleEntry;
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
