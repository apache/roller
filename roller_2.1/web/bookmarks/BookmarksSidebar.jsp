<%@ include file="/taglibs.jsp" %>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            
<div class="sidebarInner">

            <h3><fmt:message key="mainPage.actions" /></h3>
            <hr size="1" noshade="noshade" />
            
            <p>
			<%-- Add Bookmark link --%>
			<img src='<c:url value="/images/link_add.png"/>' border="0"alt="icon" />
			<roller:link page="/editor/bookmarkEdit.do">
			    <roller:linkparam id="<%= RollerRequest.FOLDERID_KEY %>"
			        name="folder" property="id" />
			    <fmt:message key="bookmarksForm.addBookmark" />
			</roller:link>
			
			</p>
			</p>
			
			<%-- Add Folder link --%>
			<img src='<c:url value="/images/folder_add.png"/>' border="0"alt="icon" />
			<roller:link page="/editor/folderEdit.do">
			    <roller:linkparam id="<%= RollerRequest.PARENTID_KEY %>"
			         name="folder" property="id" />
			    <fmt:message key="bookmarksForm.addFolder" />
			</roller:link>
			</p>

			<br />
			<br />
</div>
            
        </div>
    </div>
</div>	
