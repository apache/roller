package org.roller.pojos;

import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;

/** 
 * Base class for any object that belongs to a website.
 */
public abstract class WebsiteObject extends PersistentObject
{
    public abstract WebsiteData getWebsite();
    
    public boolean canSave() throws RollerException
    {
        Roller roller = RollerFactory.getRoller();
        if (roller.getUser().equals(UserData.SYSTEM_USER)) 
        {
            return true;
        }
        if (getWebsite().hasUserPermissions(
           roller.getUser(), (short)(PermissionsData.AUTHOR)))
        {
            return true;
        }
        return false;
    }
}

