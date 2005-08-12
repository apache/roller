<%@ include file="/taglibs.jsp" %>

<div class="sidebarfade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">
            
            <h3><fmt:message key="mainPage.actions" /></h3>
            <hr />
            
			<%-- Add Category link --%>
			<p>
            <img src='<c:url value="/images/FolderNew16.png"/>' border="0"alt="icon" />
			<roller:link page="/editor/categoryEdit.do">
			    <roller:linkparam id="<%= RollerRequest.PARENTID_KEY %>"
			         name="category" property="id" />
			    <fmt:message key="categoriesForm.addCategory" />
			</roller:link>
			</p>
			
			<br />
			<br />
			<br />
			
            </div>
        </div>
    </div>
</div>			
