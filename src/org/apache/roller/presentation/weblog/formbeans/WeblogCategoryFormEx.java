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
package org.apache.roller.presentation.weblog.formbeans;

import org.apache.roller.RollerException;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.presentation.forms.WeblogCategoryForm;

/**
 * Extends the WeblogCategoryForm so that additional properties may be added.
 * These properties are not persistent and are only needed for the UI. 
 * 
 * @struts.form name="weblogCategoryFormEx"
 */
public class WeblogCategoryFormEx extends WeblogCategoryForm
{
    private String mParentId = null;
    private boolean mMoveContents = false;
    private String mMoveToWeblogCategoryId = null;

    public WeblogCategoryFormEx()
    {
        super();
    }

    public WeblogCategoryFormEx(WeblogCategoryData catData, java.util.Locale locale) throws RollerException
    {
        super(catData, locale);
    }

    public String getParentId()
    {
        return mParentId;
    }

    public void setParentId(String parentId)
    {
        mParentId = parentId;
    }

    /** If true then contents should be moved when this folder is removed */
    public boolean getMoveContents()
    {
        return mMoveContents;
    }
    
    public void setMoveContents(boolean flag)
    {
        mMoveContents = flag;
    }

    /** WeblogCategory where contents should be moved if this cat is removed */
    public String getMoveToWeblogCategoryId()
    {
        return mMoveToWeblogCategoryId;
    }

    public void setMoveToWeblogCategoryId(String id)
    {
        mMoveToWeblogCategoryId = id;
    }
    
    /** 
     * @see org.apache.roller.presentation.forms.WeblogCategoryForm#copyFrom(org.apache.roller.pojos.WeblogCategoryData)
     */
    public void copyFrom(WeblogCategoryData dataHolder, java.util.Locale locale) throws RollerException
    {
        super.copyFrom(dataHolder, locale);
        try
        {
            mParentId = dataHolder.getParent().getId();
        }
        catch (RollerException e)
        {
            throw new RuntimeException("ERROR fetching parent category.");
        }
    }

}
