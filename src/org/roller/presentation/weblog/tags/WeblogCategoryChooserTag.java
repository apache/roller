package org.roller.presentation.weblog.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;
import org.roller.model.WeblogManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.presentation.RollerRequest;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;


/**
 * @jsp.tag name="WeblogCatagoryChooser"
 */
public class WeblogCategoryChooserTag 
	extends org.roller.presentation.tags.HybridTag
{
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(WeblogCategoryChooserTag.class);

  	//------------------------------------------------------------- 
    /**
     * Process start tag.
     * @return EVAL_SKIP_BODY
     */
    public int doStartTag( PrintWriter pw ) throws JspException 
	{
		try
		{
			HttpServletRequest req = 
				(HttpServletRequest)pageContext.getRequest();
			RollerRequest rreq = RollerRequest.getRollerRequest(
                (HttpServletRequest)pageContext.getRequest());

			WeblogManager weblogMgr = 
				rreq.getRoller().getWeblogManager(); 

			UserData user = rreq.getUser();
			List weblogCats = 
				weblogMgr.getWeblogCategories(rreq.getWebsite(), false);

			String rawUrl = req.getContextPath()+"/page/"+user.getUserName();

			pw.println("<div class=\"rWeblogCategoryChooser\">");

			Hashtable params = new Hashtable();
			params.put( RollerRequest.USERNAME_KEY, user.getUserName() );
			String weblog = RequestUtils.computeURL( 
				pageContext, 
				null,   // forward 
				rawUrl, // href
				null,   // page
				null,
				params, // params
				null,   // anchor
				false); // redirect
            String catClass = "rUnchosenCategory";
            String chosenCat = 
                    req.getParameter(RollerRequest.WEBLOGCATEGORYNAME_KEY);
            if ( chosenCat != null )
            {
                pw.println(
                    "<a href=\""+weblog+"\" class=\""+catClass+"\">"
                        + "All" +
                    "</a>");
            }
            else
            {
                catClass = "rChosenCategory";
                pw.println(
                    "<span class=\""+catClass+"\">"
                        + "All" +
                    "</span>");
            }

			for (Iterator wbcItr = weblogCats.iterator(); wbcItr.hasNext(); ) 
            {	
				WeblogCategoryData category = (WeblogCategoryData) wbcItr.next();
				String catName = category.getName();
                
                // For now don't show root category
                if (category.getParent() == null) continue;
				
				params = new Hashtable();

				String pid = pageContext.getRequest().getParameter(
					RollerRequest.PAGEID_KEY );
				if (pid != null) params.put( RollerRequest.PAGEID_KEY,pid);

				params.put( RollerRequest.WEBLOGCATEGORYNAME_KEY, catName);

				pw.println(" | ");
				weblog = RequestUtils.computeURL( 	
					pageContext, 
					null,   // forward 
					rawUrl, // href
					null,   // page
					null,
					params, // params
					null,   // anchor
					false); // redirect
    
				catClass = "rUnchosenCategory";
				chosenCat = req.getParameter(
                    RollerRequest.WEBLOGCATEGORYNAME_KEY);
				if ( chosenCat != null && chosenCat.equals(catName) )
				{
					catClass = "rChosenCategory";
					pw.println(
						"<span class=\""+catClass+"\">"
							+ category.getPath() +
						"</span>");
				} 
				else
				{
					pw.println(
						"<a href=\""+weblog+"\" class=\""+catClass+"\">"
							+ category.getPath() +
						"</a>");
				}
			}
			
			pw.println("</div>");
		}
		catch (Exception e)
		{
            mLogger.error("Exception",e);
			throw new JspException(e);
		}
		return Tag.SKIP_BODY;
    }
}

