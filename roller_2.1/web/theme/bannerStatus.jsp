<%@ include file="/taglibs.jsp" %>
<%
RollerContext rctx = RollerContext.getRollerContext();
RollerSession rollerSession = RollerSession.getRollerSession(request);
RollerRequest rreq = RollerRequest.getRollerRequest(request);
UserData user = rollerSession.getAuthenticatedUser();
WebsiteData website = rreq.getWebsite();
String absURL = rctx.getAbsoluteContextUrl(request);
boolean allowNewUsers = RollerRuntimeConfig.getBooleanProperty("users.registration.enabled");
String customRegUrl = RollerRuntimeConfig.getProperty("users.registration.url");
if(customRegUrl != null && customRegUrl.trim().equals(""))
    customRegUrl = null;
%>
<div class="bannerStatusBox">
    
    <table class="bannerStatusBox" cellpadding="0" cellspacing="0">
    <tr>
    <td class="bannerLeft">
    
        <% if (user != null) { %>
            <fmt:message key="mainPage.loggedInAs" />
            <html:link forward="yourProfile">
               <%= user.getUserName() %>
            </html:link>
        <% } %>
        
        <c:if test="${!empty model.website}" >
           - <fmt:message key="mainPage.currentWebsite" />
           <b><a href='<c:out value="${model.baseURL}" />/page/<c:out value="${model.website.handle}" />'>
               <c:out value="${model.website.handle}" />
           </a></b>
   
        </c:if>
        
    </td>

    <td class="bannerRight">
            
        <roller:link forward="main">
            <%= RollerRuntimeConfig.getProperty("site.shortName") %>
        </roller:link>

        | <roller:link forward="yourWebsites">
            <fmt:message key="mainPage.mainMenu" />
        </roller:link>

        <% if (user != null) { %>
            | <html:link forward="logout-redirect">
                <fmt:message key="navigationBar.logout"/>
            </html:link>
        <% } else { %>
            | <html:link forward="login-redirect">
                <fmt:message key="navigationBar.login"/>
            </html:link>
            
            <% if(allowNewUsers) { %>
                | <html:link forward="registerUser">
                    <fmt:message key="navigationBar.register"/>
                </html:link>
            <% } else if(customRegUrl != null) { %>
                | <a href="<%= customRegUrl %>">
                    <fmt:message key="navigationBar.register"/>
                </a>
            <% } %>
            
        <% } %>

    </td>
    </tr>
    </table>
    
</div>


