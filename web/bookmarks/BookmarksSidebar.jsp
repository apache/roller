<%@ include file="/taglibs.jsp" %>

<div class="sidebarfade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">
            
            <h3><fmt:message key="mainPage.actions" /></h3>
            <hr />
            
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
			<br />
			<br />
			<br />
            
            </div>
        </div>
    </div>
</div>	
