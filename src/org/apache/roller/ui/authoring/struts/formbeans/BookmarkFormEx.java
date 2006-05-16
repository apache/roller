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
/*
 * Created on Apr 8, 2003
 */
package org.apache.roller.ui.authoring.struts.formbeans;

import org.apache.roller.RollerException;
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.ui.authoring.struts.forms.BookmarkForm;

/**
 * Extends the BookmarkForm so that additional properties may be added.
 * These properties are not persistent and are only needed for the UI.
 *
 * @struts.form name="bookmarkFormEx"
 */
public class BookmarkFormEx extends BookmarkForm
{
    private String mFolderId = null;

    /**
     *
     */
    public BookmarkFormEx()
    {
        super();
    }

    /**
     * @param dataHolder
     */
    public BookmarkFormEx(BookmarkData dataHolder, java.util.Locale locale) throws RollerException
    {
        copyFrom(dataHolder, locale);
    }

    /**
     * @return
     */
    public String getFolderId()
    {
        return mFolderId;
    }

    /**
     * @param string
     */
    public void setFolderId(String string)
    {
        mFolderId = string;
    }

    /**
     * @see org.apache.roller.ui.authoring.struts.forms.BookmarkForm#setData(org.apache.roller.pojos.BookmarkData)
     */
    public void copyFrom(BookmarkData dataHolder, java.util.Locale locale) throws RollerException
    {
        super.copyFrom(dataHolder, locale);
        mFolderId = dataHolder.getFolder().getId();
    }
}
