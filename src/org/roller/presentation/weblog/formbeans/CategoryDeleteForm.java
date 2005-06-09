/*
 * Created on Jan 19, 2004
 */
package org.roller.presentation.weblog.formbeans;

import org.apache.struts.action.ActionForm;

import java.util.List;

/**
 * @struts.form name="categoryDeleteForm"
 */
public class CategoryDeleteForm extends ActionForm
{
    private String name;
    private String catid = null;
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
    public String getCatid()
    {
        return catid;
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
    public void setCatid(String string)
    {
        catid = string;
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
