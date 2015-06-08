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
<s:if test="actionName == 'categoryEdit'">
    <s:set var="subtitleKey">categoryForm.edit.subtitle</s:set>
    <s:set var="mainAction">categoryEdit</s:set>
</s:if>
<s:else>
    <s:set var="subtitleKey">categoryForm.add.subtitle</s:set>
    <s:set var="mainAction">categoryAdd</s:set>
</s:else>

<p class="subtitle">
    <s:text name="%{#subtitleKey}" />
</p>

<p class="pagetip">
    <s:text name="categoryForm.requiredFields">
        <s:param><s:text name="generic.name"/></s:param>
    </s:text>
</p>

<s:form action="categoryEdit!save">
    <s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:if test="actionName == 'categoryEdit'">
        <%-- bean for add does not have a bean id yet --%>
        <s:hidden name="bean.id" />
    </s:if>

    <table>
        
        <tr>
            <td><s:text name="generic.name" /></td>
            <td><s:textfield name="bean.name" size="70" maxlength="255" style="width:50%"/></td>
        </tr>
        
        <tr>
            <td><s:text name="generic.description" /></td>
            <td><s:textfield name="bean.description" size="120" style="width:50%"/></td>
        </tr>

    </table>
    
    <p>
        <s:submit value="%{getText('generic.save')}" action="%{#mainAction}!save"/>
        <s:submit value="%{getText('generic.cancel')}" action="categoryEdit!cancel" />
    </p>
    
</s:form>
