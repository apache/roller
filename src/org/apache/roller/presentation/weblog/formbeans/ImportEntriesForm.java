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
 * Created on Apr 1, 2004
 */
package org.apache.roller.presentation.weblog.formbeans;

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
