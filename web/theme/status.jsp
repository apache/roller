<%@ include file="/taglibs.jsp" %>
<%
RollerContext rctx = RollerContext.getRollerContext(request);
RollerSession rollerSession = RollerSession.getRollerSession(request);
UserData user = rollerSession.getAuthenticatedUser();
WebsiteData website = rollerSession.getCurrentWebsite();
String absURL = rctx.getAbsoluteContextUrl(request);
boolean allowNewUsers = RollerConfig.getBooleanProperty("users.registration.enabled");
%>
<table class="bannerBox">
	<tr>
	
		<td align="left">
			<% if (user != null) { %>
			    <fmt:message key="mainPage.loggedInAs" />:
                 [<b><%= user.getUserName() %></b>].
                 <% if (website != null) { %>
                    <fmt:message key="mainPage.currentWebsite" />:
                    [<b><a href='<%= absURL + "/page/" + website.getHandle() %>'><%= website.getHandle() %></a></b>]
                 <% } %> 
            <% } %>
		</td>
		
		<td align="right">
		    <roller:link forward="yourWebsites">
                <fmt:message key="mainPage.mainMenu" />
            </roller:link>
			<% if (user != null) { %>
			    | <html:link forward="logout-redirect"><fmt:message key="navigationBar.logout"/></html:link>
            <% } else if (allowNewUsers) { %>
			    | <html:link forward="login-redirect"><fmt:message key="navigationBar.login"/></html:link>
			    | <html:link forward="registerUser"><fmt:message key="navigationBar.register"/></html:link>
            <% } else { %>
			    | <html:link forward="login-redirect"><fmt:message key="navigationBar.login"/></html:link>
            <% } %>

		</td>
		
	</tr>
</table>



