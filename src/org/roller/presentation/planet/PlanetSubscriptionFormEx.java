package org.roller.presentation.planet;

import org.roller.presentation.forms.PlanetSubscriptionForm;

/**
 * @struts.form name="planetSubscriptionFormEx"
 */
public class PlanetSubscriptionFormEx 
    extends    PlanetSubscriptionForm
    implements java.io.Serializable
{
    private String groupHandle = null;
    public PlanetSubscriptionFormEx() 
    {
        super();
    }
    public String getGroupHandle() 
    {
        return groupHandle;
    }
    public void setGroupHandle(String groupHandle) 
    {
        this.groupHandle = groupHandle;
    }
}

