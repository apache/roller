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
                <roller:link page="/editor/createWebsite.do">
                    <fmt:message key="yourWebsites.createWebsite" />
                </roller:link>
			</p>
        </td>
    </tr>
</table>

<br />
