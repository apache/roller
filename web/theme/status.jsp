<%@ include file="/taglibs.jsp" %>
<%
RollerSession rollerSession = RollerSession.getRollerSession(request);
UserData user = rollerSession.getAuthenticatedUser();
WebsiteData website = rollerSession.getCurrentWebsite();
boolean allowNewUsers = RollerConfig.getBooleanProperty("users.registration.enabled");
%>

<table class="sidebarBox">
    <tr>
       <td class="sidebarBox">
          <div class="menu-tr"><div class="menu-tl">
             <fmt:message key="mainPage.status" />
          </div></div>
       </td>
    </tr>
	<tr>
		<td>
			<% if (user != null) { %>
			    <fmt:message key="mainPage.loggedInAs" />:<br />
                 [<b><%= user.getUserName() %></b>].<br />
                 <% if (website != null) { %>
                    <fmt:message key="mainPage.currentWebsite" />:<br />
                    [<b><%= website.getHandle() %></b>]<br />
                 <% } %> 
                 <br />
			    <html:link forward="logout-redirect"><fmt:message key="navigationBar.logout"/></html:link>
            <% } else if (allowNewUsers) { %>
			    <html:link forward="login-redirect"><fmt:message key="navigationBar.login"/></html:link>
                 <br /><br />
			    <html:link forward="registerUser"><fmt:message key="navigationBar.register"/></html:link>
            <% } else { %>
			    <html:link forward="login-redirect"><fmt:message key="navigationBar.login"/></html:link>
            <% } %>

		</td>
	</tr>
</table>

<br />



