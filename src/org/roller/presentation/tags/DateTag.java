package org.roller.presentation.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.RequestUtils;

/**
 * Struts-based date field tag that wraps Matt Kruze's JavaScript data chooser.
 * @jsp.tag name="Date"
 */
public class DateTag extends TagSupport
{

    // Unique key prefix keeps us from colliding with other tags and user data
    public static final String KEY_PREFIX = "ZZZ_DATETAG_ZZZ";

    private String property = null;
    private String dateFormat = null;
    private Boolean readOnly = Boolean.FALSE;

    private static Log mLogger =
        LogFactory.getFactory().getInstance(DateTag.class);

    /**
     * Renders date field by calling a JSP page.
     */
    public int doStartTag() throws JspException
    {

        // Get form name
        ActionMapping mapping =
            (ActionMapping) pageContext.getRequest().getAttribute(
                Globals.MAPPING_KEY);
        String formName = mapping.getName();

        // Get value of form field
        Object value =
            RequestUtils.lookup(pageContext, formName, property, null);
        if (value == null)
            value = "";

        // put variables into request scope for view page
        pageContext.getRequest().setAttribute(
            KEY_PREFIX + "_formName",
            formName);
        pageContext.getRequest().setAttribute(
            KEY_PREFIX + "_property",
            property);
        pageContext.getRequest().setAttribute(
            KEY_PREFIX + "_dateFormat",
            dateFormat);
        pageContext.getRequest().setAttribute(
            KEY_PREFIX + "_readOnly",
            readOnly);
        pageContext.getRequest().setAttribute(KEY_PREFIX + "_value", value);

        // dispatch to view page
        try
        {
            pageContext.include("/tags/date.jsp");
        }
        catch (Exception e)
        {
            // can't handle this here
            throw new JspException("ERROR including date.jsp");
        }

        // Don't evaluate content of tag, just continue processing this page
        return (SKIP_BODY);
    }

    /**
     * Date format string to be used.
     * 
     * @jsp.attribute required="true" rtexprvalue="true" type="java.lang.String"
     */
    public String getDateFormat()
    {
        return dateFormat;
    }

    /**
     * Name of form property represented. 
     * @jsp.attribute required="true" rtexprvalue="true" type="java.lang.String"
     */
    public String getProperty()
    {
        return property;
    }

    /**
     * True if field should be readOnly. 
     * @jsp.attribute required="false" rtexprvalue="true" type="java.lang.Boolean"
     */
    public Boolean getReadOnly()
    {
        return readOnly;
    }

    public void setDateFormat(String dateFormat)
    {
        this.dateFormat = dateFormat;
    }

    public void setProperty(String property)
    {
        this.property = property;
    }

    public void setReadOnly(Boolean readOnly)
    {
        this.readOnly = readOnly;
    }

}
