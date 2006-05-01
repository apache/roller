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
 * Created on Jan 19, 2004
 */
package org.apache.roller.presentation.weblog.formbeans;

import java.util.List;

import org.apache.struts.action.ActionForm;

/**
 * @struts.form name="categoryDeleteForm"
 */
public class CategoryDeleteForm extends ActionForm
{
    private String name;
    private String categoryId = null;
    private String moveToWeblogCategoryId = null;
    private List cats = null;
    private Boolean inUse = Boolean.FALSE;
    private Boolean confirmDelete = null;
    
    /**
     * @return
     */
    public List getCats()
    {
        return cats;
    }

    /**
     * @return
     */
    public String getCategoryId()
    {
        return categoryId;
    }

    /**
     * @return
     */
    public Boolean isInUse()
    {
        return inUse;
    }

    /**
     * @return
     */
    public String getMoveToWeblogCategoryId()
    {
        return moveToWeblogCategoryId;
    }

    /**
     * @param list
     */
    public void setCats(List list)
    {
        cats = list;
    }

    /**
     * @param string
     */
    public void setCategoryId(String string)
    {
        categoryId = string;
    }

    /**
     * @param b
     */
    public void setInUse(Boolean b)
    {
        inUse = b;
    }

    /**
     * @param string
     */
    public void setMoveToWeblogCategoryId(String string)
    {
        moveToWeblogCategoryId = string;
    }

    /**
     * @return
     */
    public Boolean isDelete()
    {
        return confirmDelete;
    }

    /**
     * @param b
     */
    public void setConfirmDelete(Boolean b)
    {
        confirmDelete = b;
    }

    /**
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param string
     */
    public void setName(String string)
    {
        name = string;
    }

    /**
     * @return Returns the delete.
     */
    public Boolean getConfirmDelete()
    {
        return confirmDelete;
    }

    /**
     * @return Returns the inUse.
     */
    public Boolean getInUse()
    {
        return inUse;
    }

}
