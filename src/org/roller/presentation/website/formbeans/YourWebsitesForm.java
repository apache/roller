package org.roller.presentation.website.formbeans;

import org.apache.struts.action.ActionForm;

/**
 * @struts.form name="yourWebsitesForm"
 * @author Dave M Johnson
 */
public class YourWebsitesForm extends ActionForm
{
    private String inviteId; 
    private String websiteId; 
    public String getWebsiteId()
    {
        return websiteId;
    }
    public void setWebsiteId(String websiteId)
    {
        this.websiteId = websiteId;
    }
    public String getInviteId()
    {
        return inviteId;
    }
    public void setInviteId(String inviteId)
    {
        this.inviteId = inviteId;
    }
}
