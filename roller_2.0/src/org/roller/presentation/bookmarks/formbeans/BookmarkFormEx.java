/*
 * Created on Apr 8, 2003
 */
package org.roller.presentation.bookmarks.formbeans;

import org.roller.RollerException;
import org.roller.pojos.BookmarkData;
import org.roller.presentation.forms.BookmarkForm;

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
     * @see org.roller.presentation.forms.BookmarkForm#setData(org.roller.pojos.BookmarkData)
     */
    public void copyFrom(BookmarkData dataHolder, java.util.Locale locale) throws RollerException
    {
        super.copyFrom(dataHolder, locale);
        mFolderId = dataHolder.getFolder().getId();
    }
}
