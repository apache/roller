package org.roller.presentation.website.formbeans;

import org.apache.struts.action.ActionForm;

/**
 * @struts.form name="yourWebsitesForm"
 * @author Dave M Johnson
 */
public class YourWebsitesForm extends ActionForm
{
    private String websiteId; 
    public String getWebsiteId()
    {
        return websiteId;
    }
    public void setWebsiteId(String websiteId)
    {
        this.websiteId = websiteId;
    }
}
