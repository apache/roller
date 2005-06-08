     /*
 * Created on Feb 27, 2004
 */
package org.roller.presentation.weblog.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.taglib.tiles.util.TagUtils;
import org.apache.struts.util.RequestUtils;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.velocity.PageHelper;
import org.roller.util.Utilities;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Apply configured PagePlugins to WeblogEntryData and display the result.
 * @jsp.tag name="ApplyPlugins"
 * @author David M Johnson
 */
public class ApplyPluginsTag extends TagSupport
{
    private static final String HELPER_KEY = "roller.pageHelper";
    private static Log mLogger =
        LogFactory.getFactory().getInstance(ApplyPluginsTag.class);

    private String name = null;
    private String property = null;
    private String scope = "request";
    
    private boolean stripHtml = false;
    private int maxLength = -1;
    private boolean skipFlag = false;

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() throws JspException
    {
        WeblogEntryData entry = 
            (WeblogEntryData)RequestUtils.lookup(pageContext, name, property, scope);
        
        String xformed = null;

        if (entry.getPlugins() != null)
        {
            // check to see if a PageHelper has already been created this request
            PageHelper helper = (PageHelper)pageContext.getRequest().getAttribute(HELPER_KEY);
            if (helper == null)
            {
                helper = loadNewPageHelper();
            }
            helper.setSkipFlag(skipFlag);
    
            xformed = helper.renderPlugins(entry);
        }
        else
        {
            xformed = entry.getText();
        }
        
        if (stripHtml)
        {
            // don't escape ampersands
            xformed = Utilities.escapeHTML( Utilities.removeHTML(xformed), false );
        }
        
        if (maxLength != -1)
        {
            xformed = Utilities.truncateNicely(xformed, maxLength, maxLength, "...");
        }
        
        // somehow things (&#8220) are getting double-escaped
        // but I cannot seem to track it down
        xformed = Utilities.stringReplace(xformed, "&amp#", "&#");
        
        try
        {
            pageContext.getOut().println(xformed);
        }
        catch (IOException e)
        {
            throw new JspException("ERROR applying plugin to entry", e);
        }
        return TagSupport.SKIP_BODY;
    }

    /**
     * PagePlugins need to be loaded and properly initialized for use.
     * Also, store the PageHelper in the Request as it will likely be
     * used more than once and this way we can skip a fair amount of overhead.
     */
    private PageHelper loadNewPageHelper()
    {
        PageHelper pageHelper = PageHelper.createPageHelper(
            (HttpServletRequest)pageContext.getRequest(), 
            (HttpServletResponse)pageContext.getResponse());

        pageContext.getRequest().setAttribute(HELPER_KEY, pageHelper);
        return pageHelper;
    }

    /**
     * Maximum length of text displayed, only applies if stripHtml is true.
     * @jsp.attribute required="false"
     * @return Returns the maxLength.
     */
    public int getMaxLength()
    {
        return maxLength;
    }

    /**
     * Maximum length of text displayed, only applies if stripHtml is true.
     * @param maxLength The maxLength to set.
     */
    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
    }

    /**
     * Set to true to strip all HTML markup from output.
     * @jsp.attribute required="false"
     * @return Returns the stripHtml.
     */
    public boolean getStripHtml()
    {
        return stripHtml;
    }

    /**
     * Set to true to strip all HTML markup from output.
     * @param stripHtml The stripHtml to set.
     */
    public void setStripHtml(boolean stripHtml)
    {
        this.stripHtml = stripHtml;
    }

    /**
     * Set to true to inform PagePlugins if they
     * should "skip" themselves.
     *
     * @jsp.attribute required="false"
     * @return Returns the skipFlag.
     */
    public boolean getSkipFlag()
    {
        return skipFlag;
    }

    /**
     * Set to true to inform PagePlugins if they
     * should "skip" themselves.
     * @param skipFlag The skipFlag to set.
     */
    public void setSkipFlag(boolean skipFlag)
    {
        this.skipFlag = skipFlag;
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
