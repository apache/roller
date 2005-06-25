
package org.roller.pojos;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.model.RollerFactory;

/** 
 * Base class for all of Roller's persistent objects.
 */
public abstract class PersistentObject implements Serializable
{
	private long mTimeStamp = 0L; // this was only for Castor, right? -Lance

	public PersistentObject()
	{
	}

	/** Setter needed by RollerImpl.storePersistentObject() */
	public abstract void setData( PersistentObject vo );

    /** Get ID */
	public abstract String getId();

    /** Set ID */
	public abstract void setId( String id );

	//---------------------------------------------------------- TimeStampable


    public void save() throws RollerException 
    {
        PersistenceStrategy pstrategy =
            RollerFactory.getRoller().getPersistenceStrategy();
        pstrategy.store(this);
    }
    
    public void remove() throws RollerException 
    {
        PersistenceStrategy pstrategy =
            RollerFactory.getRoller().getPersistenceStrategy();
        pstrategy.remove(this);
    }
    
    
    public String toString() 
    {
        try 
        {
            // this may throw an exception if called by a thread that
            return ToStringBuilder.reflectionToString(
                this, ToStringStyle.MULTI_LINE_STYLE);
        }
        catch (Throwable e)
        {
            // alternative toString() implementation used in case of exception
            return getClass().getName() + ":" + getId();
        }
    }

    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }
    
    /** Can user associated with persistence session save this object? */
    public boolean canSave() throws RollerException
    {
        return true;
    }
}

