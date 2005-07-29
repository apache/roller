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
			<%-- Add Category link --%>
			<img src='<c:url value="/images/FolderNew16.png"/>' border="0"alt="icon" />
			<roller:link page="/editor/categoryEdit.do">
			    <roller:linkparam id="<%= RollerRequest.PARENTID_KEY %>"
			         name="category" property="id" />
			    <fmt:message key="categoriesForm.addCategory" />
			</roller:link>
			</p>
        </td>
    </tr>
</table>

<br />
