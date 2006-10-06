<!--
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
-->
<%@ include file="/taglibs.jsp" %>


<p class="subtitle">
<c:if test="${state == 'add'}">
    <fmt:message key="categoryForm.add.subtitle" />
    
</c:if>
<c:if test="${state == 'edit'}">
    <fmt:message key="categoryForm.edit.subtitle" />
</c:if>
<c:if test="${state == 'correct'}">
    <fmt:message key="categoryForm.correct.subtitle" />
</c:if>
</p>

<p>
<b><fmt:message key="categoriesForm.path" /></b>:
<c:if test="${!empty parentCategory.path}">
    <c:out value="${parentCategory.path}" />
</c:if>
<c:if test="${empty parentCategory.path}">
    /
</c:if>
</p>

<html:form action="/roller-ui/authoring/categorySave" method="post" focus="name">

    <html:hidden property="method" name="method" value="update"/>
    <html:hidden property="id" />
    <html:hidden property="parentId" />

    <table>

    <tr>
        <td><fmt:message key="categoryForm.name" /></td>
        <td><html:text property="name" size="70" maxlength="255" /></td>
    </tr>

    <tr>
        <td><fmt:message key="categoryForm.description" /></td>
        <td><html:textarea property="description" rows="5" cols="50" /></td>
    </tr>

    <tr>
        <td><fmt:message key="categoryForm.image" /></td>
        <td><html:textarea property="image" rows="5" cols="50" /></td>
    </tr>

    </table>
    
    <p>
    <input type="submit" value="<fmt:message key='categoryForm.save' />" />
    <c:url var="categoriesUrl" value="/roller-ui/authoring/categories.do">
       <c:param name="method" value="selectCategory" />
       <c:param name="weblog" value="${model.website.handle}" />
       <c:param name="categoryId" value="${requestScope.parentCategory.id}" />
    </c:url>
    <input type="button" value="<fmt:message key='categoryForm.cancel' />" 
        onclick="window.location = '<c:out value="${categoriesUrl}" />'" />
    </p>

</html:form>

