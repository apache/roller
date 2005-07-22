/*
 * Template.java
 *
 * Created on June 27, 2005, 11:59 AM
 */

package org.roller.pojos;

import java.util.Date;


/**
 * The Template interface represents the abstract concept of a single unit
 * of templated or non-rendered content.  For Roller we mainly think of
 * templates as Velocity templates which are meant to be fed into the 
 * Velocity rendering engine.
 *
 * @author Allen Gilliland
 */
public interface Template {
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getId();
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getName();
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getDescription();
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getContents();
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getLink();
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public Date getLastModified();
    
    /*
    public void setId(String id);
    public void setName(String name);
    public void setDescription(String desc);
    public void setContents(String contents);
    public void setLink(String link);
    public void setLastModified(Date date);
    */
}
