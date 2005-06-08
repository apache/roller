
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
	private long mTimeStamp = 0L;

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
    
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.MULTI_LINE_STYLE);
    }

    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public int hashCode(Object o) {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}

