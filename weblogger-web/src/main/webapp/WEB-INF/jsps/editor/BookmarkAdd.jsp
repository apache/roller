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
    <s:text name="bookmarkForm.add.subtitle" >
        <s:param value="folder.path" />
    </s:text>
</p>

<s:form action="bookmarkAdd!save">
    <s:hidden name="weblog" />
    <s:hidden name="folderId" />
    
    <table>
        
        <tr>
            <td><s:text name="bookmarkForm.name" /></td>
            <td><s:textfield name="bean.name" maxlength="255" size="70" /></td>
        </tr>
        
        <tr>
            <td><s:text name="bookmarkForm.description" /></td>
            <td><s:textarea name="bean.description" rows="5" cols="50" /></td>
        </tr>
        
        <tr>
            <td><s:text name="bookmarkForm.url" /></td>                
            <td><s:textfield name="bean.url" maxlength="255" size="70" /></td>
        </tr>
        
        <tr>
            <td><s:text name="bookmarkForm.rssUrl" /></td>         
            <td><s:textfield name="bean.feedUrl" maxlength="255" size="70" /></td>
        </tr>
        
        <tr>
            <td><s:text name="bookmarkForm.image" /></td>          
            <td><s:textfield name="bean.image" maxlength="255" size="70" /></td>
        </tr>
        
        <tr>
            <td><s:text name="bookmarkForm.priority" /></td>         
            <td><s:textfield name="bean.priority" maxlength="255" size="5" /></td>
        </tr>
        
        <tr>
            <td><s:text name="bookmarkForm.weight" /></td>          
            <td><s:textfield name="bean.weight" maxlength="255" size="5" /></td>
        </tr>
        
    </table>
    
    <p>
        <s:submit value="%{getText('bookmarkForm.save')}" />
        <s:submit value="%{getText('bookmarkForm.cancel')}" action="bookmarks" />
    </p>
    
</s:form>
