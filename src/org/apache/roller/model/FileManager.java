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
package org.apache.roller.model;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.roller.RollerException;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.RollerMessages;

/**
 * Interface for managing files uploaded to Roller.
 *
 * NOTE: this should probably be renamed "ResourceManager" or similar
 * since the real jobe here is managing resources, not just files.  We should
 * then extend this a bit more to include the notion of not only user uploaded
 * resources, but also other resources the system stores, such as the blacklist.
 *
 * @author dave
 */
public interface FileManager extends Serializable 
{
    /** Determine if file can be saved in website's file space. */
    public boolean canSave(
        String weblogHandle, String name, long size, RollerMessages msgs) 
        throws RollerException;
    
    /** Get website's files */
    public File[] getFiles(String weblogHandle) 
        throws RollerException;
    
    /** Delete specified file from website's file space. */
    public void deleteFile(String weblogHandle, String name) 
        throws RollerException;

    /** Save file in website's file space or throw exception if rules violated. */
    public void saveFile(String weblogHandle, String name, long size, InputStream is) 
        throws RollerException;

    /** Return true if weblog is over the file-upload limit */
    public boolean overQuota(String weblogHandle) throws RollerException; 
            
    /**
     * Get directory in which uploaded files are stored
     */
    public String getUploadDir();
    /**
     * Get base URL where uploaded files are made available.
     */
    public String getUploadUrl();
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
}
