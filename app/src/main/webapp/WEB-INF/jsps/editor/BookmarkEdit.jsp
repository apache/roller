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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<%-- Titling, processing actions different between add and edit --%>
<s:if test="actionName == 'bookmarkEdit'">
    <s:set var="subtitleKey">bookmarkForm.edit.subtitle</s:set>
    <s:set var="mainAction">bookmarkEdit</s:set>
</s:if>
<s:else>
    <s:set var="subtitleKey">bookmarkForm.add.subtitle</s:set>
    <s:set var="mainAction">bookmarkAdd</s:set>
</s:else>

<p class="subtitle">
    <s:text name="%{#subtitleKey}" >
        <s:param value="bookmark.folder.name"/>
    </s:text>
</p>

<p class="pagetip">
    <s:text name="bookmarkForm.requiredFields">
        <s:param><s:text name="generic.name"/></s:param>
        <s:param><s:text name="bookmarkForm.url"/></s:param>
    </s:text>
</p>

<s:form>
	<s:hidden name="salt" />
    <s:hidden name="weblog" />

    <s:if test="actionName == 'bookmarkEdit'">
        <%-- bean for bookmark add does not have a bean id yet --%>
        <s:hidden name="formBean.id" />
    </s:if>

    <table>
        
        <tr>
            <td><s:text name="generic.name" /></td>
            <td><s:textfield name="formBean.name" maxlength="255" size="70" style="width:50%" onBlur="this.value=this.value.trim()"/></td>
        </tr>
        
        <tr>
            <td><s:text name="bookmarkForm.url" /></td>
            <td><s:textfield name="formBean.url" maxlength="255" size="70" style="width:50%" onBlur="this.value=this.value.trim()"/></td>
        </tr>
        
        <tr>
            <td><s:text name="generic.description" /></td>
            <td><s:textfield name="formBean.description" maxlength="255" size="70" style="width:50%" onBlur="this.value=this.value.trim()"/></td>
        </tr>

    </table>
    
    <p>
        <s:submit value="%{getText('generic.save')}" action="%{#mainAction}!save"/>
        <s:submit value="%{getText('generic.cancel')}" action="bookmarkEdit!cancel" />
    </p>
    
</s:form>
