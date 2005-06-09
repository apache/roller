
package org.roller.presentation.weblog.formbeans;

import org.roller.RollerException;
import org.roller.pojos.WeblogCategoryData;
import org.roller.presentation.forms.WeblogCategoryForm;


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

