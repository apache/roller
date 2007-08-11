package org.apache.roller.weblogger.business;

import java.util.Date;
import org.apache.roller.weblogger.business.runnable.RollerTaskWithLeasing;

public class TestTask extends RollerTaskWithLeasing {
    
    public TestTask() {
        
    }

    public String getName() {
        return "TestTask";
    }

    public String getClientId() {
        return "TestTaskClientId";
    }

    public Date getStartTime(Date current) {
        return current;
    }

    public String getStartTimeDesc() {
        return "immediate";
    }

    public int getLeaseTime() {
        return 300;
    }

    public int getInterval() {
        return 1800;
    }

    public void runTask() {
    }
}
