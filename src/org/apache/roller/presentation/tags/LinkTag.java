/*
 * $Header: /cvs/roller/roller/src/org/roller/presentation/tags/LinkTag.java,v 1.3 2004/09/23 02:15:36 snoopdave Exp $
 * $Revision: 1.3 $
 * $Date: 2004/09/23 02:15:36 $
 *
 * ====================================================================
 */
package org.apache.roller.presentation.tags;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Map;
import javax.servlet.jsp.JspException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Category;
import org.apache.struts.taglib.TagUtils;
import org.apache.struts.util.RequestUtils;
import org.apache.struts.util.ResponseUtils;

/**
 * Generates an HTML link. This class adds the parameter feature to the Struts
 * html link tag. <br/>It should be use as follow:
 * 
 * <pre>
 * 
 *   &lt;hm:link href=&quot;http://java.sun.com&quot;&gt;
 *     &lt;hm:linkparam name=&quot;action&quot; value=&quot;submit&quot; /&gt;
 *     &lt;hm:linkparam name=&quot;ref&quot; value=&quot;144532&quot; /&gt;
 *   &lt;/hm:linl&gt;
 *   
 * </pre>
 * 
 * This will produce the equivalent of
 * 
 * <pre>
 * &lt;href=&quot;http://java.sun.com?_action=submit&amp;ref=144532&quot;&gt;
 * </pre>
 * 
 * Title: BSquare Description: Bsquare Projects Copyright: Copyright (c) 2001
 * Company: HubMethods
 * 
 * @author $Author: snoopdave $
 * @version $Revision: 1.3 $
 */
public class LinkTag extends org.apache.struts.taglib.html.LinkTag
{
    // ------------------------------------------------------ Logging
    static Category cat = Category.getInstance(LinkTag.class);
    // ------------------------------------------------------ Instance Vartables
    /**
     * The full HREF URL
     */
    private StringBuffer hrefURL = new StringBuffer();

    // ------------------------------------------------------------- Properties
    //--------------------------------------------------------- Public Methods
    /**
     * Intialize the hyperlink.
     * 
     * @exception JspException
     *                if a JSP exception has occurred
     */
    public int doStartTag() throws JspException
    {
        // Special case for name anchors
        if (linkName != null)
        {
            StringBuffer results = new StringBuffer("<a name=\"");
            results.append(linkName);
            results.append("\">");
            return (EVAL_BODY_TAG);
        }
        // Generate the hyperlink URL
        Map params = RequestUtils.computeParameters(pageContext, paramId,
                        paramName, paramProperty, paramScope, name, property,
                        scope, transaction);
        String url = null;
        try
        {
            url = RequestUtils.computeURL(pageContext, forward, href, page,
                            params, anchor, false);
        }
        catch (MalformedURLException e)
        {
            RequestUtils.saveException(pageContext, e);
            throw new JspException(messages.getMessage("rewrite.url", e
                            .toString()));
        }
        // Generate the opening anchor element
        hrefURL = new StringBuffer("<a href=\"");
        hrefURL.append(url);
        if (cat.isDebugEnabled())
            cat.debug("hrefURL = '" + hrefURL.toString());
        // Evaluate the body of this tag
        this.text = null;
        return (EVAL_BODY_TAG);
    }

    /**
     * Add a new parameter to the request
     * 
     * @param name
     *            the name of the request parameter
     * @param value
     *            the value of the request parameter
     */
    public void addRequestParameter(String name, String value)
    {
        if (cat.isDebugEnabled())
            cat.debug("Adding '" + name + "' with value '" + value + "'");
        boolean question = (hrefURL.toString().indexOf('?') >= 0);
        if (question)
        { // There are request parameter already
            hrefURL.append('&');
        }
        else
            hrefURL.append('?');
        hrefURL.append(name);
        hrefURL.append('=');
        hrefURL.append(URLEncoder.encode(value));
        if (cat.isDebugEnabled())
            cat.debug("hrefURL = '" + hrefURL.toString() + "'");
    }

    /**
     * Render the href reference
     * 
     * @exception JspException
     *                if a JSP exception has occurred
     */
    public int doEndTag() throws JspException
    {
        hrefURL.append("\"");
        if (target != null)
        {
            hrefURL.append(" target=\"");
            hrefURL.append(target);
            hrefURL.append("\"");
        }
        hrefURL.append(prepareStyles());
        hrefURL.append(prepareEventHandlers());
        hrefURL.append(">");
        if (text != null)
            hrefURL.append(text);
        hrefURL.append("</a>");
        if (cat.isDebugEnabled())
            cat.debug("Total request is = '" + hrefURL.toString() + "'");
        // Print this element to our output writer
        ResponseUtils.write(pageContext, hrefURL.toString());
        return (EVAL_PAGE);
    }

    /**
     * Release any acquired resources.
     */
    public void release()
    {
        super.release();
        forward = null;
        href = null;
        name = null;
        property = null;
        target = null;
    }

    // ----------------------------------------------------- Protected Methods
    /**
     * Return the specified hyperlink, modified as necessary with optional
     * request parameters.
     * 
     * @exception JspException
     *                if an error occurs preparing the hyperlink
     */
    protected String hyperlink() throws JspException
    {
        String href = this.href;
        // If "forward" was specified, compute the "href" to forward to
//        if (forward != null)
//        {
//            ActionForwards forwards = (ActionForwards) pageContext
//                            .getAttribute(Action.FORWARDS_KEY,
//                                            PageContext.APPLICATION_SCOPE);
//            ActionMapping mapping = (ActionMapping)pageContext.getAttribute(ActionConfig.)            
//            if (forwards == null)
//                throw new JspException(messages.getMessage("linkTag.forwards"));
//            ActionForward forward = forwards.findForward(this.forward);
//            if (forward == null)
//                throw new JspException(messages.getMessage("linkTag.forward"));
//            HttpServletRequest request = (HttpServletRequest) pageContext
//                            .getRequest();
//            href = request.getContextPath() + forward.getPath();
//        }
        // Just return the "href" attribute if there is no bean to look up
        if ((property != null) && (name == null))
            throw new JspException(messages.getMessage("getter.name"));
        if (name == null)
            return (href);
        // Look up the map we will be using
        Object bean = pageContext.findAttribute(name);
        if (bean == null)
            throw new JspException(messages.getMessage("getter.bean", name));
        Map map = null;
        if (property == null)
        {
            try
            {
                map = (Map) bean;
            }
            catch (ClassCastException e)
            {
                throw new JspException(messages.getMessage("linkTag.type"));
            }
        }
        else
        {
            try
            {
                map = (Map) PropertyUtils.getProperty(bean, property);
                if (map == null)
                    throw new JspException(messages.getMessage(
                                    "getter.property", property));
            }
            catch (IllegalAccessException e)
            {
                throw new JspException(messages.getMessage("getter.access",
                                property, name));
            }
            catch (InvocationTargetException e)
            {
                Throwable t = e.getTargetException();
                throw new JspException(messages.getMessage("getter.result",
                                property, t.toString()));
            }
            catch (ClassCastException e)
            {
                throw new JspException(messages.getMessage("linkTag.type"));
            }
            catch (NoSuchMethodException e)
            {
                throw new JspException(messages.getMessage("getter.method",
                                property, name));
            }
        }
            // Append the required query parameters
//        StringBuffer sb = new StringBuffer(href);
//        boolean question = (href.indexOf("?") >= 0);
//        Iterator keys = map.keySet().iterator();
//        while (keys.hasNext())
//        {
//            String key = (String) keys.next();
//            Object value = map.get(key);
//            if (value instanceof String[])
//            {
//                String values[] = (String[]) value;
//                for (int i = 0; i < values.length; i++)
//                {
//                    if (question)
//                        sb.append('&');
//                    else
//                    {
//                        sb.append('?');
//                        question = true;
//                    }
//                    sb.append(key);
//                    sb.append('=');
//                    sb.append(URLEncoder.encode(values[i]));
//                }
//            }
//            else
//            {
//                if (question)
//                    sb.append('&');
//                else
//                {
//                    sb.append('?');
//                    question = true;
//                }
//                sb.append(key);
//                sb.append('=');
//                sb.append(URLEncoder.encode(value.toString()));
//            }
//        }
//        // Return the final result
//        return (sb.toString());
        try
        {
            return TagUtils.getInstance().computeURL(
                            pageContext,
                            forward,
                            href,
                            null, 
                            null,
                            null,
                            map,
                            null,
                            false
                            );
        }
        catch (MalformedURLException e)
        {
            throw new JspException(e);
        }
    }
}
