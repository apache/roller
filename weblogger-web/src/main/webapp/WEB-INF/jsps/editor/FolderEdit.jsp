<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<p class="subtitle">
    <s:text name="folderForm.edit.subtitle" >
        <s:param value="folder.path" />
    </s:text>
</p>

<s:form action="folderEdit!save">
    <s:hidden name="weblog" />
    <s:hidden name="bean.id" />
    
    <%-- if we cancel then we need this attribute --%>
    <s:hidden name="folderId" value="%{folder.parent.id}" />
    
    <table>
        <tr>
            <td><s:text name="folderForm.name" /></td>
            <td><s:textfield name="bean.name" size="70" maxlength="255" /></td>
        </tr>
        
        <tr>
            <td><s:text name="folderForm.description" /></td>
            <td><s:textarea name="bean.description" rows="5" cols="50" /></td>
        </tr>
    </table>
    
    <p>
        <s:submit value="%{getText('folderForm.save')}" />
        <s:submit value="%{getText('folderForm.cancel')}" action="bookmarks" />
    </p>
    
</s:form>
