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

<%-- JavaScript for bookmarks table --%> 
<script type="text/javascript">
// <!-- 
function onDelete() 
{
    if ( confirm("<s:text name='bookmarksForm.delete.confirm' />") ) 
    {
        document.bookmarks.method.value = "deleteSelected";
        document.bookmarks.submit();
    }
 }
function onMove() 
{
    if ( confirm("<s:text name='bookmarksForm.move.confirm' />") ) 
    {
        document.bookmarks.method.value = "moveSelected";
        document.bookmarks.submit();
    }
}
// -->
</script>

<s:if test="folder.parent == null">
    <p class="subtitle">
        <s:text name="bookmarksForm.subtitle" >
            <s:param value="weblog" />
        </s:text>
    </p>  
    <p class="pagetip">
        <s:text name="bookmarksForm.rootPrompt" />
    </p>
</s:if>
<s:else>
    <p class="subtitle">
    <s:text name="bookmarksForm.path" />: <s:text name="%{folder.name}" />
</s:else>

<%-- Form is a table of folders followed by bookmarks, each with checkbox --%>
<s:form action="bookmarks!delete">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:hidden name="folderId" /> 
    
    <s:if test="!allFolders.isEmpty">

        <%-- View button --%>
        <s:submit type="button" action="bookmarks!view" key="bookmarksForm.viewFolder" />

        <%-- Folder to View combo-box --%>
        <s:select name="viewFolderId" list="allFolders" listKey="id" listValue="name" />

        <br /><br />

    </s:if>
    <table class="rollertable">
    
        <tr class="rHeaderTr">
            <th class="rollertable" width="5%"><input name="control" type="checkbox" onclick="toggleFunctionAll(this.checked);"/></th>
            <th class="rollertable" width="5%">&nbsp;</th>
            <th class="rollertable" width="30%"><s:text name="bookmarksForm.name" /></th>
            <th class="rollertable" width="45%"><s:text name="bookmarksForm.description" /></th>
            <th class="rollertable" width="5%"><s:text name="bookmarksForm.priority" /></th>
            <th class="rollertable" width="5%"><s:text name="bookmarksForm.edit" /></th>
            <th class="rollertable" width="5%"><s:text name="bookmarksForm.visitLink" /></th>
        </tr>
        
        <s:if test="folder.folders.size > 0 || folder.bookmarks.size > 0">
        
        <%-- Folders --%>
        <s:iterator id="folder" value="folder.folders" status="rowstatus">
            <s:if test="#rowstatus.odd == true">
                <tr class="rollertable_odd">
            </s:if>
            <s:else>
                <tr class="rollertable_even">
            </s:else>
                
                <td class="rollertable center" style="vertical-align:middle">
                    <input type="checkbox" name="selectedFolders" value="<s:property value="#folder.id"/>" />
                </td>
                
                <td class="rollertable" align="center"><img src='<s:url value="/images/folder.png"/>' alt="icon" /></td>
                
                <td class="rollertable">
                    <s:url id="folderUrl" action="bookmarks">
                        <s:param name="weblog" value="%{actionWeblog.handle}" />
                        <s:param name="folderId" value="#folder.id" />
                    </s:url>
                    <s:a href="%{folderUrl}"><str:truncateNicely lower="15" upper="20" ><s:property value="#folder.name" /></str:truncateNicely></s:a>
                </td>
                
                <td class="rollertable">
                    <str:truncateNicely lower="30" upper="35" > </str:truncateNicely>
                </td>
                
                <td class="rollertable"></td>
                
                <td class="rollertable" align="center">
                    <s:url id="editUrl" action="folderEdit">
                        <s:param name="weblog" value="%{actionWeblog.handle}" />
                        <s:param name="bean.id" value="#folder.id" />
                    </s:url>
                    <s:a href="%{editUrl}"><img src='<s:url value="/images/page_white_edit.png"/>' border="0" alt="icon" /></s:a>
                </td>
                
                <td class="rollertable">&nbsp;</td>
                
            </tr>
        </s:iterator>
        
        <%-- Bookmarks --%>
        <s:iterator id="bookmark" value="folder.bookmarks" status="rowstatus">
            <s:if test="#rowstatus.odd == true">
                <tr class="rollertable_odd">
            </s:if>
            <s:else>
                <tr class="rollertable_even">
            </s:else>
                
                <td class="rollertable center" style="vertical-align:middle">
                    <input type="checkbox" name="selectedBookmarks" value="<s:property value="#bookmark.id"/>" />
                </td>
                
                <td class="rollertable" align="center"><img src='<s:url value="/images/link.png"/>' alt="icon" /></td>
                
                <td class="rollertable">
                    <str:truncateNicely lower="15" upper="20" ><s:property value="#bookmark.name" /></str:truncateNicely>
                </td>
                
                <td class="rollertable">
                    <str:truncateNicely lower="30" upper="35" ><s:property value="#bookmark.url" /></str:truncateNicely>
                </td>
                
                <td class="rollertable">
                    &nbsp;<s:property value="#bookmark.priority" />
                </td>
                
                <td class="rollertable" align="center">
                    <s:url id="editUrl" action="bookmarkEdit">
                        <s:param name="weblog" value="%{actionWeblog.handle}" />
                        <s:param name="bean.id" value="#bookmark.id" />
                    </s:url>
                    <s:a href="%{editUrl}"><img src='<s:url value="/images/page_white_edit.png"/>' border="0" alt="icon" 
                             title="<s:text name='bookmarksForm.edit.tip' />" /></s:a>
                </td>
                
                <td class="rollertable" align="center">
                    <s:if test="#bookmark.url != null" >
                        <a href="<s:property value="#bookmark.url" />">
                            <img src='<s:url value="/images/world_go.png"/>' border="0" alt="icon" 
                                 title="<s:text name='bookmarksForm.visitLink.tip' />" />
                        </a>
                    </s:if>
                </td>
                
            </tr>
        </s:iterator>
        
        </s:if>
        <s:else>
            <tr>
                <td style="vertical-align:middle" colspan="7"><s:text name="bookmarksForm.noresults" /></td>
            </tr>
        </s:else>
    </table>
    
    <div class="control">
        <s:if test="folder.folders.size > 0 || folder.bookmarks.size > 0">
                <%-- Delete-selected button --%>
                <input type="button" value="<s:text name="bookmarksForm.delete"/>" onclick="onDelete()" />
        </s:if>

        <s:if test="!allFolders.isEmpty && ( folder.folders.size > 0 || folder.bookmarks.size > 0)">
            <%-- Move-selected button --%>
            <s:submit type="button" action="bookmarks!move" key="bookmarksForm.move" onclick="onMove();return false;" />

            <%-- Move-to combo-box --%>
            <s:select name="targetFolderId" list="allFolders" listKey="id" listValue="name" />
        </s:if>
    </div>

</s:form>
