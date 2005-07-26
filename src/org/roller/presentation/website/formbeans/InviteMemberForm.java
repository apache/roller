package org.roller.presentation.website.formbeans;

import org.apache.struts.action.ActionForm;

/**
 * @struts.form name="inviteMemberForm"
 * @author Dave M Johnson
 */
public class InviteMemberForm extends ActionForm
{
    private String userName;
    public String getUserName()
    {
        return userName;
    }
    public void setUserName(String userName)
    {
        this.userName = userName;
    }
}
