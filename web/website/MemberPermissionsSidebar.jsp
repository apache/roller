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
                <roller:link page="/editor/inviteMember.do">
                    <fmt:message key="memberPermissions.inviteMember" />
                </roller:link>
			</p>
        </td>
    </tr>
</table>

<br />
