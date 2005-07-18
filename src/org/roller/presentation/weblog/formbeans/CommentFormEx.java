package org.roller.presentation.weblog.formbeans;

import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.roller.RollerException;
import org.roller.pojos.CommentData;
import org.roller.presentation.forms.CommentForm;

/**
 * Extends the WeblogEntryForm so that additional properties may be added.
 * These properties are not persistent and are only needed for the UI.
 *
 * @struts.form name="commentFormEx"
 * @author Lance Lavandowska
 */
public class CommentFormEx extends CommentForm
{
    private String[] deleteComments = null;
    private String[] spamComments = null;
    private String mEntryId = null;

    public CommentFormEx()
    {
        super();
    }

    public CommentFormEx(CommentData entryData, java.util.Locale locale ) throws RollerException
    {
        super(entryData, locale);
    }

    public String[] getDeleteComments()
    {
        return deleteComments;
    }

    public void setDeleteComments(String[] deleteIds)
    {
        deleteComments = deleteIds;
    }

    /**
     * @return
     */
    public String[] getSpamComments()
    {
        return spamComments;
    }

    public void setSpamComments(String[] spamIds)
    {
        spamComments = spamIds;
    }

    /**
     * @return
     */
    public String getWeblogEntryId()
    {
        return mEntryId;
    }

    /**
     * @param string
     */
    public void setWeblogEntryId(String string)
    {
        mEntryId = string;
    }

    /**
     * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
     */
    public void reset(ActionMapping mapping, HttpServletRequest request)
    {
        super.reset(mapping, request);
        deleteComments = null;
        spamComments = null;
    }

    /**
     * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping, javax.servlet.ServletRequest)
     */
    public void reset(ActionMapping mapping, ServletRequest request)
    {
        super.reset(mapping, request);
        deleteComments = null;
        spamComments = null;
    }
    
    public void copyTo(org.roller.pojos.CommentData dataHolder, Locale locale) 
        throws RollerException
    {
        super.copyTo(dataHolder, locale);
        if (getSpam() == null) dataHolder.setSpam(Boolean.FALSE);
        if (getNotify() == null) dataHolder.setNotify(Boolean.FALSE);
    }
}

