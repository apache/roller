/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
package org.apache.roller.weblogger.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds collection of error messages and collection of status messages.
 * @author David M Johnson
 */
public class RollerMessages
{
    private final List<RollerMessage> mErrors = new ArrayList<>();
    private final List<RollerMessage> mMessages = new ArrayList<>();
    
    public RollerMessages() 
    {
    }
    public void addError(String key)
    {
        mErrors.add(new RollerMessage(key, null));
    }
    public void addError(String key, String arg)
    {
        mErrors.add(new RollerMessage(key, new String[]{arg}));
    }
    public void addError(String key, String[] args)
    {
        mErrors.add(new RollerMessage(key, args));
    }
    public void addMessage(String key)
    {
        mMessages.add(new RollerMessage(key, null));
    }
    public void addMessage(String key, String arg)
    {
        mMessages.add(new RollerMessage(key, new String[]{arg}));
    }
    public void addMessage(String key, String[] args)
    {
        mMessages.add(new RollerMessage(key, args));
    }
    public Iterator<RollerMessage> getErrors()
    {
        return mErrors.iterator();
    }
    public Iterator<RollerMessage> getMessages()
    {
        return mMessages.iterator();
    }
    public int getErrorCount() 
    {
        return mErrors.size();
    }
    public int getMessageCount() 
    {
        return mMessages.size();
    }
    @Override
    public String toString() 
    {
        StringBuilder sb = new StringBuilder();
        for (RollerMessage msg : mMessages) {
            sb.append(msg.getKey());
            sb.append(" : ");
        }
        for (RollerMessage msg : mErrors) {
            sb.append(msg.getKey());
            sb.append(" : ");
        }
        return sb.toString();
    }
    public static class RollerMessage
    {
        private String mKey;
        private String[] mArgs;
        public RollerMessage(String key, String[] args)
        {
            mKey = key;
            mArgs = args;
        }
        public String[] getArgs()
        {
            return mArgs;
        }
        public void setArgs(String[] args)
        {
            mArgs = args;
        }
        public String getKey()
        {
            return mKey;
        }
        public void setKey(String key)
        {
            mKey = key;
        }
    }
}
