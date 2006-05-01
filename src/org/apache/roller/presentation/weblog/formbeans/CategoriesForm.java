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
 * @struts.form name="categoriesForm"
 */ 
public class CategoriesForm extends WeblogCategoryForm
{
    private String mParentId = null;
	private boolean mMoveContents = false; 
	private String mMoveToCategoryId = null; 
    private String[] mSelectedCategories = null;

	public CategoriesForm()
	{
		super();
	}

	public CategoriesForm( WeblogCategoryData catData, java.util.Locale locale ) throws RollerException
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

    //-------------------------------------------------- Property moveContents

	/** If true then contents should be moved when this Category is removed */
	public boolean getMoveContents() { return mMoveContents; }

	/** If true then contents should be moved when this Category is removed */
	public void setMoveContents( boolean flag ) { mMoveContents = flag;}

    //----------------------------------------------- Property moveToCategoryId

	/** Category where contents should be moved if this Category is removed */ 
	public String getMoveToCategoryId() { return mMoveToCategoryId; }

	/** Category where contents should be moved if this Category is removed */ 
	public void setMoveToCategoryId( String id ) { mMoveToCategoryId = id;}

    //--------------------------------------------- Property selectedCategories 

    /** Get selected Categories */
    public String[] getSelectedCategories() { return mSelectedCategories; }

    /** Set selected Categories */
    public void setSelectedCategories( String[] f ) {mSelectedCategories = f;}
}

