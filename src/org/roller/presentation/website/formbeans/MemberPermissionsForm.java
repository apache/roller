package org.roller.presentation.website.formbeans;

import org.apache.struts.action.ActionForm;

/**
 * @struts.form name="memberPermissionsForm"
 * @author Dave M Johnson
 */
public class MemberPermissionsForm extends ActionForm
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
