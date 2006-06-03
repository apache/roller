/*
 * Created on Apr 1, 2004
 */
package org.roller.presentation.weblog.formbeans;

import java.util.ArrayList;

import org.apache.struts.action.ActionForm;

/**
 * @struts.form name="importEntries"
 * @author lance.lavandowska
 */
public class ImportEntriesForm extends ActionForm
{
    private ArrayList xmlFiles = new ArrayList();
    private String importFileName;
    
    /**
     * @return Returns the xmlFiles.
     */
    public ArrayList getXmlFiles()
    {
        return this.xmlFiles;
    }
    
    /**
     * @param xmlFiles The xmlFiles to set.
     */
    public void setXmlFiles(ArrayList xmlFiles)
    {
        this.xmlFiles = xmlFiles;
    }
    
    /**
     * @return Returns the importFileName.
     */
    public String getImportFileName()
    {
        return this.importFileName;
    }
    
    /**
     * @param importFileName The importFileName to set.
     */
    public void setImportFileName(String importFileName)
    {
        this.importFileName = importFileName;
    }
}
