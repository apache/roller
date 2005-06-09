package org.roller.model;

import org.roller.RollerException;

/**
 * Interface for pluggable scheduled task.
 * @author David M Johnson
 */
public interface ScheduledTask
{
    public void init(Roller roller, String realPath) throws RollerException;
}
