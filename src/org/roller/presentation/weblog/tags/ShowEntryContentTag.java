/* Created on Feb 27, 2004 */
package org.roller.presentation.weblog.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Map;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;
import org.apache.velocity.VelocityContext;

import org.roller.model.Roller;
import org.roller.model.PagePluginManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.RollerContext;
import org.roller.util.Utilities;
import org.roller.pojos.wrapper.WeblogEntryDataWrapper;

/**
 * Shows entry text with plugins applied.
 * @jsp.tag name="ShowEntryContent"
 * @author David M Johnson
 */
public class ShowEntryContentTag extends TagSupport {
    static final long serialVersionUID = 3166731504235428544L;
    private static Log mLogger =
            LogFactory.getFactory().getInstance(ShowEntrySummaryTag.class);
    
    private String name = null;
    private String property = null;
    private String scope = "request";
    
    private boolean stripHtml = false;
    private int maxLength = -1;
    private boolean singleEntry = false;
    
    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException {
        Roller roller = RollerFactory.getRoller();
        WeblogEntryData entry =
            (WeblogEntryData)RequestUtils.lookup(pageContext, name, property, scope);
        
        String xformed = null;
        
        if (entry.getPlugins() != null) {
            RollerContext rctx = 
                RollerContext.getRollerContext();
            try {
                PagePluginManager ppmgr = roller.getPagePluginManager();
                Map plugins = ppmgr.createAndInitPagePlugins(
                        entry.getWebsite(),
                        rctx.getServletContext(),
                        rctx.getAbsoluteContextUrl(),
                        new VelocityContext());
                xformed = ppmgr.applyPagePlugins(
                              entry, plugins, entry.getText(), singleEntry);
                
            } catch (Exception e) {
                mLogger.error(e);
            }
        } else {
            xformed = entry.getText();
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
        
        try {
            pageContext.getOut().println(xformed);
        } catch (IOException e) {
            throw new JspException("ERROR applying plugin to entry", e);
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
