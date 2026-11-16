/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.roller.weblogger.business;

import java.util.Date;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.runnable.RollerTaskWithLeasing;

public class TestTask extends RollerTaskWithLeasing {
    public static final String NAME = "TestTask";

    public TestTask() {}

    public void init() throws WebloggerException {
        this.init(TestTask.NAME);
    }

    @Override
    public String getClientId() {
        return "TestTaskClientId";
    }

    @Override
    public Date getStartTime(Date current) {
        return current;
    }

    @Override
    public String getStartTimeDesc() {
        return "immediate";
    }

    @Override
    public int getLeaseTime() {
        return 300;
    }

    @Override
    public int getInterval() {
        return 1800;
    }

    @Override
    public void runTask() {
    }

}
