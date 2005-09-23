<%@ include file="/taglibs.jsp" %>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            
            <h3><fmt:message key="mainPage.actions" /></h3>
            <hr size="1" noshade="noshade" />
            <img src='<c:url value="/images/ComposeMail16.gif"/>' alt="mail-icon" align="bottom" />
            <c:url value="/editor/inviteMember.do" var="inviteUrl">
               <c:param name="weblog" value="${model.website.handle}" />
            </c:url>
            <a href='<c:out value="${inviteUrl}" />'>
                <fmt:message key="memberPermissions.inviteMember" />
            </a>
            <br />
            <fmt:message key="memberPermissions.whyInvite" />       
			<br />
			<br />
					
        </div>
    </div>
</div>	

<br />
<br />

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">
            
            <h3>
                <fmt:message key="memberPermissions.permissionsHelpTitle" />
            </h3>
            <hr size="1" noshade="noshade" />
            
            <fmt:message key="memberPermissions.permissionHelp" />	
		    <br />
		    <br />
		    
            </div>
        </div>
    </div>
</div>	


