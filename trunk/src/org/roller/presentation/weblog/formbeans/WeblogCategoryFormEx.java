package org.roller.presentation.weblog.formbeans;

import org.roller.RollerException;
import org.roller.pojos.WeblogCategoryData;
import org.roller.presentation.forms.WeblogCategoryForm;

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
     * @see org.roller.presentation.forms.WeblogCategoryForm#copyFrom(org.roller.pojos.WeblogCategoryData)
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
