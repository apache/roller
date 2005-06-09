package org.roller.presentation.tags;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.config.RollerConfig;
import org.roller.presentation.RollerContext;
import org.roller.presentation.util.SslUtil;

/**
 * This tag library is designed to be used on a JSP to switch HTTP -> HTTPS
 * protocols and vise versa.
 * 
 * If you want to force the page to be viewed in SSL, then you would do
 * something like this: <br />
 * <br />
 * 
 * <pre>
 *  &lt;tag:secure /&gt;
 *  or
 *  &lt;tag:secure mode=&quot;secured&quot; /&gt;
 * </pre>
 * 
 * If you want the force the page to be viewed in over standard http, then you
 * would do something like: <br />
 * 
 * <pre>
 *  &lt;tag:secure mode=&quot;unsecured&quot; /&gt;
 * </pre>
 * 
 * @jsp.tag name="secure" bodycontent="empty"
 * @author <a href="mailto:jon.lipsky@xesoft.com">Jon Lipsky</a>
 * 
 * Contributed by:
 * XEsoft GmbH Oskar-Messter-Strasse 18 85737 Ismaning, Germany
 * http://www.xesoft.com
 */
public class SecureTag extends BodyTagSupport
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(SecureTag.class);

    //~ Static fields/initializers
    // =============================================
    public static final String MODE_SECURED = "secured";
    public static final String MODE_UNSECURED = "unsecured";
    public static final String MODE_EITHER = "either";
    //~ Instance fields
    // ========================================================
    private Log log = LogFactory.getLog(SecureTag.class);
    protected String TAG_NAME = "Secure";
    private String mode = MODE_SECURED;
    private String httpPort = null;
    private String httpsPort = null;
    private String httpsHeaderName = null;
    private String httpsHeaderValue = null;

    //~ Methods
    // ================================================================
    /**
     * Sets the mode attribute. This is included in the tld file.
     * 
     * @jsp.attribute description="The mode attribute (secure | unsecured)"
     *                required="false" rtexprvalue="true"
     */
    public void setMode(String aMode)
    {
        mode = aMode;
    }

    public int doStartTag() throws JspException
    {
        // get the port numbers
        ServletContext ctx = pageContext.getServletContext();
        httpPort = RollerConfig.getProperty("securelogin.http.port");
        if (httpPort == null)
        {
            httpPort = SslUtil.STD_HTTP_PORT;
        }
        httpsPort = RollerConfig.getProperty("securelogin.https.port");
        if (httpsPort == null)
        {
            httpsPort = SslUtil.STD_HTTPS_PORT;
        }
        httpsHeaderName = RollerConfig.getProperty("securelogin.https.headername");
        httpsHeaderValue = RollerConfig.getProperty("securelogin.https.headervalue");
        return SKIP_BODY;
    }

    public int doAfterBody() throws JspException
    {
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException
    {
        HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
        if (mode.equalsIgnoreCase(MODE_SECURED)) 
        {
            if (!isSecure((HttpServletRequest)pageContext.getRequest())) 
            {
                String vQueryString = req.getQueryString();
                String vPageUrl = req.getRequestURI();
                String vServer = req.getServerName();
                StringBuffer vRedirect = new StringBuffer("");
                vRedirect.append("https://");
                if (httpsPort == null || httpsPort.trim().length()==0 
                        || httpsPort.equals(SslUtil.STD_HTTPS_PORT))                 
                {
                    vRedirect.append(vServer + vPageUrl);
                }
                else 
                {
                    vRedirect.append(vServer + ":" + httpsPort + vPageUrl);
                }
                if (vQueryString != null)
                {
                    vRedirect.append("?");
                    vRedirect.append(vQueryString);
                }
                if (log.isDebugEnabled())
                {
                    log.debug("attempting to redirect to: " + vRedirect);
                }
                try
                {
                    ((HttpServletResponse) pageContext.getResponse())
                                    .sendRedirect(vRedirect.toString());
                    return SKIP_PAGE;
                }
                catch (Exception exc2)
                {
                    mLogger.error(exc2);
                    throw new JspException(exc2);
                }
            }
        }
        else if (mode.equalsIgnoreCase(MODE_UNSECURED))
        {
            if (isSecure((HttpServletRequest)pageContext.getRequest()))
            {
                String vQueryString = req.getQueryString();
                String vPageUrl = req.getRequestURI();
                String vServer = req.getServerName();
                StringBuffer vRedirect = new StringBuffer("");
                vRedirect.append("http://");
                if (!httpPort.equals(SslUtil.STD_HTTP_PORT))
                {
                    vRedirect.append(vServer + ":" + httpPort + vPageUrl);
                }
                else 
                {
                    vRedirect.append(vServer + vPageUrl);
                }
                if (vQueryString != null)
                {
                    vRedirect.append("?");
                    vRedirect.append(vQueryString);
                }
                try
                {
                    ((HttpServletResponse) pageContext.getResponse())
                                    .sendRedirect(vRedirect.toString());
                    return SKIP_PAGE;
                }
                catch (Exception exc2)
                {
                    throw new JspException(exc2.getMessage());
                }
            }
        }
        else if (mode.equalsIgnoreCase(MODE_EITHER))
        {
            return EVAL_PAGE;
        }
        else
        {
            throw new JspException("Illegal value for the attribute mode: "
                            + mode);
        }
        return EVAL_PAGE;
    }
    
    /** 
     * Test for HTTPS connection by using request.isSecure() or, 
     * if httpsHeaderName is set, test for reqest header instead.
     * If httpsHeaderValue is also set, test for that specific value.
     */
    private boolean isSecure(HttpServletRequest request) 
    {
        boolean secure = false;
        if (httpsHeaderName == null) 
        {
            secure = request.isSecure();
        }
        else
        {
            String headerValue = request.getHeader(httpsHeaderName);
            if (headerValue != null && headerValue.trim().length() > 0)
            {
                secure = httpsHeaderValue==null || httpsHeaderValue.equals(headerValue); 
            }
        }
        mLogger.debug("Connection secure="+secure);
        return secure;
    }
}