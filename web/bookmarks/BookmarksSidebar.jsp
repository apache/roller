<%@ include file="/taglibs.jsp" %>

<table class="sidebarBox" >
    <tr>
       <td class="sidebarBox">
          <div class="menu-tr"><div class="menu-tl">
             <fmt:message key="mainPage.actions" />
          </div></div>
       </td>
    </tr>    
    <tr>
        <td>
        
            <p>
			<%-- Add Bookmark link --%>
			<img src='<c:url value="/images/BookmarkNew16.png"/>' border="0"alt="icon" />
			<roller:link page="/editor/bookmarkEdit.do">
			    <roller:linkparam id="<%= RollerRequest.FOLDERID_KEY %>"
			        name="folder" property="id" />
			    <fmt:message key="bookmarksForm.addBookmark" />
			</roller:link>
			
			</p>
			</p>
			
			<%-- Add Folder link --%>
			<img src='<c:url value="/images/FolderNew16.png"/>' border="0"alt="icon" />
			<roller:link page="/editor/folderEdit.do">
			    <roller:linkparam id="<%= RollerRequest.PARENTID_KEY %>"
			         name="folder" property="id" />
			    <fmt:message key="bookmarksForm.addFolder" />
			</roller:link>
			</p>
            
        </td>
    </tr>
</table>

<br />
