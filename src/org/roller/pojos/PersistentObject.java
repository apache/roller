
package org.roller.pojos;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.roller.RollerException;


/**
 * Base class for all of Roller's persistent objects.
 */
public abstract class PersistentObject implements Serializable {
    
    
    /**
     * All persistent objects require an identifier.
     */
    public abstract String getId();
    
    
    public abstract void setId( String id );
    
    
    /**
     * Load data based on data from another object.
     */
    public abstract void setData(PersistentObject obj);
    
    
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }
    
    
    // TODO: how efficient is this?
    public String toString() {
        try {
            // this may throw an exception if called by a thread that
            return ToStringBuilder.reflectionToString(
                    this, ToStringStyle.MULTI_LINE_STYLE);
        } catch (Throwable e) {
            // alternative toString() implementation used in case of exception
            return getClass().getName() + ":" + getId();
        }
    }
    
}

