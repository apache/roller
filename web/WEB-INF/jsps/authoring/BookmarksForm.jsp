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
<% pageContext.setAttribute("leftPage","/bookmarks/BookmarksSidebar.jsp"); %>

<%-- JavaScript for bookmarks table --%> 
<script type="text/javascript">
<!-- 
function setChecked(val) 
{
    len = document.bookmarksForm.elements.length;
    var i=0;
    for( i=0 ; i<len ; i++) 
    {
        document.bookmarksForm.elements[i].checked=val;
    }
}
function onDelete() 
{
    if ( confirm("Delete selected bookmarks?") ) 
    {
        document.bookmarksForm.method.value = "deleteSelected";
        document.bookmarksForm.submit();
    }
 }
function onMove() 
{
    if ( confirm("Move selected bookmarks?") ) 
    {
        document.bookmarksForm.method.value = "moveSelected";
        document.bookmarksForm.submit();
    }
}
//-->
</script>

<c:choose>
    <c:when test="${empty model.folderPath}">
       <p class="subtitle">
           <fmt:message key="bookmarksForm.subtitle" >
               <fmt:param value="${model.website.handle}" />
           </fmt:message>
       </p>  
       <p class="pagetip">
           <fmt:message key="bookmarksForm.rootPrompt" />
       </p>
    </c:when>
    
    <c:otherwise>
        <p class="subtitle">
        <fmt:message key="bookmarksForm.path" />:
        <c:forEach var="loopfolder" items="${model.folderPath}">
            /
            <roller:link page="/roller-ui/authoring/bookmarks.do">
                <roller:linkparam id="method" value="selectFolder" />
                <roller:linkparam 
                    id="<%= RequestConstants.FOLDER_ID %>" 
                    name="loopfolder" property="id" />
                <c:out value="${loopfolder.name}" />
            </roller:link>
        </c:forEach>
        <p>
        <p><fmt:message key="bookmarksForm.folderPrompt" /></p>
    </c:otherwise>
</c:choose>

<%-- Form is a table of folders followed by bookmarks, each with checkbox --%>

<html:form action="/roller-ui/authoring/bookmarks" method="post">
<input type="hidden" name="method" /> 
<html:hidden property="folderId" /> 

<%-- Select-all button --%>
<input type="button" value="<fmt:message key='bookmarksForm.checkAll' />" 
   onclick="setChecked(1)" /></input>

<%-- Select-none button --%>
<input type="button" value="<fmt:message key='bookmarksForm.checkNone' />" 
   onclick="setChecked(0)" /></input>

<%-- Delete-selected button --%>
<input type="button" value="<fmt:message key='bookmarksForm.delete' />" 
   onclick="onDelete()"/></input>

<c:if test="${!empty model.allFolders}">
    <%-- Move-selected button --%>
    <input type="button" value="<fmt:message key='bookmarksForm.move' />"   
       onclick="onMove()"/></input>

    <%-- Move-to combo-box --%>
    <html:select property="moveToFolderId" size="1">
        <html:options collection="allFolders" 
            property="id" labelProperty="path"/>
    </html:select>
</c:if>
<p />

<table class="rollertable">

    <tr class="rHeaderTr">
        <th class="rollertable" width="5%">&nbsp;</th>
        <th class="rollertable" width="5%">&nbsp;</th>
        <th class="rollertable" width="30%"><fmt:message key="bookmarksForm.name" /></th>
        <th class="rollertable" width="50%"><fmt:message key="bookmarksForm.description" /></th>
        <th class="rollertable" width="5%"><fmt:message key="bookmarksForm.edit" /></th>
        <th class="rollertable" width="5%"><fmt:message key="bookmarksForm.visitLink" /></th>
    </tr>

    <%-- Folders --%>
    <c:forEach var="loopfolder" items="${model.folder.folders}" >
        <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">

            <td class="rollertable">
                <html:multibox property="selectedFolders">
                    <c:out value="${loopfolder.id}" />
                </html:multibox>
            </td>


            <td class="rollertable" align="center"><img src='<c:url value="/images/folder.png"/>' alt="icon" /></td>
            
            <td class="rollertable">
               <roller:link page="/roller-ui/authoring/bookmarks.do">
                   <roller:linkparam id="method" value="selectFolder" />
                   <roller:linkparam 
                       id="<%= RequestConstants.FOLDER_ID %>" 
                       name="loopfolder" property="id" />
                   <str:truncateNicely lower="15" upper="20" ><c:out value="${loopfolder.name}" /></str:truncateNicely>
               </roller:link>
            </td>

            <td class="rollertable">
                <str:truncateNicely lower="30" upper="35" ><c:out value="${loopfolder.description}" /></str:truncateNicely>
            </td>

            <td class="rollertable" align="center">
               <roller:link forward="folderEdit">
                   <roller:linkparam 
                       id="<%= RequestConstants.FOLDER_ID %>" 
                       name="loopfolder" property="id" />
                   <img src='<c:url value="/images/page_white_edit.png"/>' border="0" alt="icon" />
               </roller:link>
            </td>

            <td class="rollertable">&nbsp;</td>

        </roller:row>
    </c:forEach>

    <%-- Bookmarks --%>
    <c:forEach var="loopbookmark" items="${model.folder.bookmarks}" >
        <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">

            <td class="rollertable">
                <html:multibox property="selectedBookmarks">
                    <c:out value="${loopbookmark.id}" />
                </html:multibox>
            </td>

            <td class="rollertable" align="center"><img src='<c:url value="/images/link.png"/>' alt="icon" /></td>
            
            <td class="rollertable">
                <str:truncateNicely lower="15" upper="20" ><c:out value="${loopbookmark.name}" /></str:truncateNicely>
            </td>

            <td class="rollertable">
                <str:truncateNicely lower="30" upper="35" ><c:out value="${loopbookmark.description}" /></str:truncateNicely>
            </td>

            <td class="rollertable" align="center">
               <roller:link page="/roller-ui/authoring/bookmarkEdit.do">
                   <roller:linkparam 
                       id="<%= RequestConstants.BOOKMARK_ID %>" 
                       name="loopbookmark" property="id" />                   
                   <img src='<c:url value="/images/page_white_edit.png"/>' border="0" alt="icon" 
                       title="<fmt:message key='bookmarksForm.edit.tip' />" />
               </roller:link>
            </td>

            <td class="rollertable" align="center">
                <c:if test="${!empty loopbookmark.url}" >
                   <a href="<c:out value='${loopbookmark.url}'/>">
                       <img src='<c:url value="/images/world_go.png"/>' border="0" alt="icon" 
                           title="<fmt:message key='bookmarksForm.visitLink.tip' />" />
                   </a>
                </c:if>
            </td>

        </roller:row>
    </c:forEach>

</table>

</html:form>
