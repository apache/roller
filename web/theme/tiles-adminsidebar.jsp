<%@ include file="/taglibs.jsp" %><%
RollerContext rctx = RollerContext.getRollerContext(request);
RollerSession rollerSession = RollerSession.getRollerSession(request);
UserData user = rollerSession.getAuthenticatedUser();
WebsiteData website = rollerSession.getCurrentWebsite();
String absURL = rctx.getAbsoluteContextUrl(request);
boolean allowNewUsers = RollerConfig.getBooleanProperty("users.registration.enabled");
%>

<div class="sidebarfade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">
            
                <h3><fmt:message key="mainPage.status" /></h3>
                <hr />
                <c:if test="${!empty model.rollerSession.authenticatedUser}" >
                    <fmt:message key="mainPage.loggedInAs" />:
                     [<b> <c:out value="${model.rollerSession.authenticatedUser.userName}" /></b>]
                </c:if>
                <p>
                    <img align="center" src='<c:url value="/images/TipOfTheDay16.gif"/>' />
                    <fmt:message key="mainPage.adminWarning" />
                </p>

			<br />
			<br />
					
            </div>
        </div>
    </div>
</div>	

<br />



