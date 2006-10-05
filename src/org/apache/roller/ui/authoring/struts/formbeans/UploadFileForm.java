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

package org.apache.roller.ui.authoring.struts.formbeans;

import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.upload.FormFile;


/**
 * Form holds data for 5 uploaded files and array of file names of files to be deleted.
 * @struts.form name="uploadFiles"
 */
public class UploadFileForm
        extends org.apache.struts.action.ActionForm {
    
    // TODO: can file-upload be improved to allow an arbitrary number of files?
    protected transient FormFile upload0 = null;
    protected transient FormFile upload1 = null;
    protected transient FormFile upload2 = null;
    protected transient FormFile upload3 = null;
    protected transient FormFile upload4 = null;
    protected transient FormFile upload5 = null;
    protected transient FormFile upload6 = null;
    protected transient FormFile upload7 = null;
    protected transient FormFile upload8 = null;
    protected transient FormFile upload9 = null;
    
    protected String[] deleted;
    
    protected String path = null;
    
    
    public UploadFileForm() {
        super();
    }
    
    public FormFile[] getUploadedFiles() {
        ArrayList formFiles = new ArrayList();
        if (upload0 != null && !StringUtils.isEmpty(upload0.getFileName())) 
            formFiles.add(upload0);
        if (upload1 != null && !StringUtils.isEmpty(upload1.getFileName())) 
            formFiles.add(upload1);
        if (upload2 != null && !StringUtils.isEmpty(upload2.getFileName())) 
            formFiles.add(upload2);
        if (upload3 != null && !StringUtils.isEmpty(upload3.getFileName())) 
            formFiles.add(upload3);
        if (upload4 != null && !StringUtils.isEmpty(upload4.getFileName())) 
            formFiles.add(upload4);
        if (upload5 != null && !StringUtils.isEmpty(upload5.getFileName())) 
            formFiles.add(upload5);
        if (upload6 != null && !StringUtils.isEmpty(upload6.getFileName())) 
            formFiles.add(upload6);
        if (upload7 != null && !StringUtils.isEmpty(upload7.getFileName())) 
            formFiles.add(upload7);
        if (upload8 != null && !StringUtils.isEmpty(upload8.getFileName())) 
            formFiles.add(upload8);
        if (upload9 != null && !StringUtils.isEmpty(upload9.getFileName())) 
            formFiles.add(upload9);
        return (FormFile[])formFiles.toArray(new FormFile[formFiles.size()]);
    }
    
    public void setUploadedFile0(FormFile file) { upload0 = file; }
    public void setUploadedFile1(FormFile file) { upload1 = file; }
    public void setUploadedFile2(FormFile file) { upload2 = file; }
    public void setUploadedFile3(FormFile file) { upload3 = file; }
    public void setUploadedFile4(FormFile file) { upload4 = file; }    
    public void setUploadedFile5(FormFile file) { upload5 = file; }
    public void setUploadedFile6(FormFile file) { upload6 = file; }
    public void setUploadedFile7(FormFile file) { upload7 = file; }
    public void setUploadedFile8(FormFile file) { upload8 = file; }
    public void setUploadedFile9(FormFile file) { upload9 = file; }
    
    public FormFile getUploadedFile0() { return upload0; }
    public FormFile getUploadedFile1() { return upload1; }
    public FormFile getUploadedFile2() { return upload2; }
    public FormFile getUploadedFile3() { return upload3; }
    public FormFile getUploadedFile4() { return upload4; }   
    public FormFile getUploadedFile5() { return upload5; }
    public FormFile getUploadedFile6() { return upload6; }
    public FormFile getUploadedFile7() { return upload7; }
    public FormFile getUploadedFile8() { return upload8; }
    public FormFile getUploadedFile9() { return upload9; }
    
    public void setDeleteFiles(String[] fileNames) {
        deleted = fileNames;
    }
    
    public String[] getDeleteFiles() { return deleted; }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

