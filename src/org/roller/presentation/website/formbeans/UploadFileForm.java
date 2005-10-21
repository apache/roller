
package org.roller.presentation.website.formbeans;

import org.apache.struts.upload.FormFile;


/**
 * Allows the user to upload a set of files.
 * @struts.form name="uploadFiles"
 */
public class UploadFileForm
    extends    org.apache.struts.action.ActionForm
{
    //protected FormFile[] mUploadedFiles = null;
    protected transient FormFile mUploadedFile = null;

    // The name of file to be deleted
    protected String[] mDeleteFiles;

    public UploadFileForm()
    {
        super();
    }

    //------------------------------------------------- Property uploadedFiles

    /** files to be written to disk */
    //public void setUploadedFiles(FormFile[] files) { mUploadedFiles = files; }

    /** files to be written to disk */
    //public FormFile[] getUploadedFiles() { return mUploadedFiles; }

    /** files to be written to disk */
    public void setUploadedFile(FormFile file) { mUploadedFile = file; }

    /** files to be written to disk */
    public FormFile getUploadedFile() { return mUploadedFile; }

    /** files to be deleted from disk **/
    public void setDeleteFiles(String[] fileNames)
    {
        mDeleteFiles = fileNames;
    }

    /** files to be deleted from disk **/
    public String[] getDeleteFiles() { return mDeleteFiles; }
}

