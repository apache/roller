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
<%@ include file="/WEB-INF/jsps/taglibs.jsp" %>

<%-- JavaScript for categories table --%> 
<script type="text/javascript">
<!-- 
function setChecked(val) 
{
    len = document.categoriesForm.elements.length;
    var i=0;
    for( i=0 ; i<len ; i++) 
    {
        document.categoriesForm.elements[i].checked=val;
    }
}
function onMove() 
{
    if ( confirm("<fmt:message key='categoriesForm.move.confirm' />") ) 
    {
        document.categoriesForm.method.value = "moveSelected";
        document.categoriesForm.submit();
    }
}
//-->
</script>

<c:choose>
    <c:when test="${empty model.categoryPath}">
    <p class="subtitle">
        <fmt:message key="categoriesForm.subtitle" >
            <fmt:param value="${model.website.handle}" />
        </fmt:message>
    </p>  
    <p class="pagetip">
        <fmt:message key="categoriesForm.rootPrompt" />
    </p> 
    </c:when>
    
    <c:otherwise>
        <p>
        <b><fmt:message key="categoriesForm.path" /></b>:
        <c:forEach var="loopcategory" items="${model.categoryPath}">
            /
            <roller:link page="/roller-ui/authoring/categories.do">
                <roller:linkparam id="method" value="selectCategory" />
                <roller:linkparam 
                    id="<%= RequestConstants.WEBLOGCATEGORY_ID %>" 
                    name="loopcategory" property="id" />
                <c:out value="${loopcategory.name}" />
            </roller:link>
        </c:forEach>
        </p>
        <p><fmt:message key="categoriesForm.categoryPrompt" /></p>
    </c:otherwise>
</c:choose>


<%-- Form is a table of categories each with checkbox --%>

<html:form action="/roller-ui/authoring/categories" method="post">
<input type="hidden" name="method" /> 
<input type="hidden" name="weblog" value='<c:out value="${model.website.handle}" />' /> 
<html:hidden property="parentId" /> 

<%-- Select-all button --%>
<input type="button" value="<fmt:message key='categoriesForm.checkAll' />" 
   onclick="setChecked(1)" /></input>

<%-- Select-none button --%>
<input type="button" value="<fmt:message key='categoriesForm.checkNone' />" 
   onclick="setChecked(0)" /></input>

</td>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

<%-- Move-selected button --%>
<input type="button" value="<fmt:message key='categoriesForm.move' />"   
   onclick="onMove()"/></input>

<%-- Move-to combo-box --%>
<html:select property="moveToCategoryId" size="1">
    <html:options collection="allCategories" 
        property="id" labelProperty="path"/>
</html:select>

<p />

<br />

<table class="rollertable">

    <tr class="rollertable">
        <th class="rollertable" width="5%">&nbsp;</td>
        <th class="rollertable" width="5%">&nbsp;</td>
        <th class="rollertable" width="30%"><fmt:message key="categoriesForm.name" /></td>
        <th class="rollertable" width="45%"><fmt:message key="categoriesForm.description" /></td>
        <th class="rollertable" width="5%"><fmt:message key="categoriesForm.edit" /></td>
        <th class="rollertable" width="5%"><fmt:message key="categoriesForm.remove" /></td>
    </tr>

    <%-- Categories --%>
    <c:forEach var="loopcategory" items="${model.category.weblogCategories}" >
        <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">

            <td class="rollertable">
                <html:multibox property="selectedCategories">
                    <c:out value="${loopcategory.id}" />
                </html:multibox>
            </td>


            <td class="rollertable" align="center"><img src='<c:url value="/images/folder.png"/>' alt="icon" /></td>
            
            <td class="rollertable">
               <roller:link page="/roller-ui/authoring/categories.do">
                   <roller:linkparam id="method" value="selectCategory" />
                   <roller:linkparam 
                       id="<%= RequestConstants.WEBLOGCATEGORY_ID %>" 
                       name="loopcategory" property="id" />
                   <str:truncateNicely lower="15" upper="20" ><c:out value="${loopcategory.name}" /></str:truncateNicely>
               </roller:link>
            </td>

            <td class="rollertable">
                <str:truncateNicely lower="30" upper="35" ><c:out value="${loopcategory.description}" /></str:truncateNicely>
            </td>

            <td class="rollertable" align="center">
               <roller:link page="/roller-ui/authoring/categoryEdit.do">
                   <roller:linkparam 
                       id="<%= RequestConstants.WEBLOGCATEGORY_ID %>" 
                       name="loopcategory" property="id" />
                   <img src='<c:url value="/images/page_white_edit.png"/>' border="0" alt="icon" />
               </roller:link>
            </td>

            <td class="rollertable" align="center">
               <roller:link page="/roller-ui/authoring/categoryDelete.do">
                   <roller:linkparam 
	                   id="<%= RequestConstants.WEBLOGCATEGORY_ID %>" 
	                   name="loopcategory" property="id" />
                   <roller:linkparam 
	                   id="method" value="deleteSelected" />
                   <img src='<c:url value="/images/delete.png"/>' border="0" alt="icon" />
               </roller:link>
            </td>

        </roller:row>
    </c:forEach>

</table>

</html:form>

