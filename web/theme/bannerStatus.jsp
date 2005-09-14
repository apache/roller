<%@ include file="/taglibs.jsp" %>
<%
RollerContext rctx = RollerContext.getRollerContext(request);
RollerSession rollerSession = RollerSession.getRollerSession(request);
RollerRequest rreq = RollerRequest.getRollerRequest(request);
UserData user = rollerSession.getAuthenticatedUser();
WebsiteData website = rreq.getWebsite();
String absURL = rctx.getAbsoluteContextUrl(request);
boolean allowNewUsers = RollerConfig.getBooleanProperty("users.registration.enabled");
%>
<div class="bannerStatusBox">
    
    <div class="bannerLeft">
    
        <% if (user != null) { %>
            <fmt:message key="mainPage.loggedInAs" />
             <b><%= user.getUserName() %></b>
        <% } %>
        
        <c:if test="${!empty model.website}" >
           | <fmt:message key="mainPage.currentWebsite" />
           <b><a href='<c:out value="${model.baseURL}" />/page/<c:out value="${model.website.handle}" />'>
               <c:out value="${model.website.handle}" />
           </a></b>
   
        </c:if>
        
    </div>

    <div class="bannerRight">
            
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
        <% } else if (allowNewUsers) { %>
            | <html:link forward="login-redirect">
                <fmt:message key="navigationBar.login"/>
            </html:link>
            | <html:link forward="registerUser">
                <fmt:message key="navigationBar.register"/>
            </html:link>
        <% } else { %>
            | <html:link forward="login-redirect">
                <fmt:message key="navigationBar.login"/>
            </html:link>
        <% } %>

    </div>
    
</div>



