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
<script>
function onDelete()
{
    if ( confirm("<s:text name='bookmarksForm.delete.confirm' />") ) 
    {
        document.bookmarks.submit();
    }
}

function onDeleteFolder()
{
    if ( confirm("<s:text name='bookmarksForm.deleteFolder.confirm' />") )
    {
        document.bookmarks.action='<s:url action="bookmarks!deleteFolder" />';
        document.bookmarks.submit();
    }
}

function onMove()
{
    if ( confirm("<s:text name='bookmarksForm.move.confirm' />") ) 
    {
        document.bookmarks.action='<s:url action="bookmarks!move" />';
        document.bookmarks.submit();
    }
}
</script>

<s:if test="folder.name == 'default'">
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
    <s:url var="editUrl" action="folderEdit">
        <s:param name="weblog" value="%{actionWeblog.handle}" />
        <s:param name="bean.id" value="%{folder.id}" />
        <s:param name="folderId" value="%{folder.id}" />
    </s:url>
    <s:a href="%{editUrl}"><img src='<s:url value="/images/page_white_edit.png"/>' border="0" alt="icon" /
        title="<s:text name='bookmarksForm.folder.edit.tip' />" /></s:a>
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
            <th class="rollertable" width="5%"><input name="control" type="checkbox" onclick="toggleFunctionAll(this.checked);"
                title="<s:text name="bookmarksForm.selectAllLabel"/>"/></th>
            <th class="rollertable" width="25%"><s:text name="generic.name" /></th>
            <th class="rollertable" width="25%"><s:text name="bookmarksForm.url" /></th>
            <th class="rollertable" width="35%"><s:text name="bookmarksForm.feedurl" /></th>
            <th class="rollertable" width="5%"><s:text name="generic.edit" /></th>
            <th class="rollertable" width="5%"><s:text name="bookmarksForm.visitLink" /></th>
        </tr>
        
        <s:if test="folder.bookmarks.size > 0">
        
        <%-- Bookmarks --%>
        <s:iterator id="bookmark" value="folder.bookmarks" status="rowstatus">
            <s:if test="#rowstatus.odd == true">
                <tr class="rollertable_odd">
            </s:if>
            <s:else>
                <tr class="rollertable_even">
            </s:else>
                
                <td class="rollertable center" style="vertical-align:middle">
                    <input type="checkbox" name="selectedBookmarks"
                    title="<s:text name="bookmarksForm.selectOneLabel"><s:param value="#bookmark.name"/></s:text>"
                    value="<s:property value="#bookmark.id"/>" />
                </td>
                
                <td class="rollertable">
                    <str:truncateNicely lower="25" upper="30" ><s:property value="#bookmark.name" /></str:truncateNicely>
                </td>
                
                <td class="rollertable">
                    <str:truncateNicely lower="40" upper="50" ><s:property value="#bookmark.url" /></str:truncateNicely>
                </td>
                
                <td class="rollertable">
                    <str:truncateNicely lower="60" upper="70" ><s:property value="#bookmark.feedUrl" /></str:truncateNicely>
                </td>

                <td class="rollertable" align="center">
                    <s:url var="editUrl" action="bookmarkEdit">
                        <s:param name="weblog" value="%{actionWeblog.handle}" />
                        <s:param name="bean.id" value="#bookmark.id" />
                        <s:param name="folderId" value="%{folderId}" suppressEmptyParameters="true"/>
                    </s:url>
                    <s:a href="%{editUrl}"><img src='<s:url value="/images/page_white_edit.png"/>' border="0" alt="icon" 
                             title="<s:text name='bookmarksForm.edit.tip' />" /></s:a>
                </td>
                
                <td class="rollertable" align="center">
                    <s:if test="#bookmark.url != null" >
                        <a href="<s:property value="#bookmark.url" />">
                            <img src='<s:url value="/images/world_go.png"/>' border="0" alt="icon" title="<s:text name='bookmarksForm.visitLink.tip' />" />
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
    
    <div class="control clearfix">
        <s:if test="folder.bookmarks.size > 0">
                <%-- Delete-selected button --%>
                <input type="button" value="<s:text name="bookmarksForm.delete"/>" onclick="onDelete();return false;" />
        </s:if>

        <s:if test="!allFolders.isEmpty && folder.bookmarks.size > 0">
            <%-- Move-selected button --%>
            <s:submit value="%{getText('bookmarksForm.move')}" action="bookmarks!move" onclick="onMove();return false;" />

            <%-- Move-to combo-box --%>
            <s:select name="targetFolderId" list="allFolders" listKey="id" listValue="name" />
        </s:if>

        <s:if test="folder.name != 'default'">
            <span style="float:right">
                <s:submit value="%{getText('bookmarksForm.deleteFolder')}" action="bookmarks!deleteFolder" onclick="onDeleteFolder();return false;"/>
            </span>
        </s:if>
    </div>

</s:form>
