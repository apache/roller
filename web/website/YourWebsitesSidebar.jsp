<%@ include file="/taglibs.jsp" %>

<div class="sidebarfade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">
            
             <h3><fmt:message key="yourWebsites.shortCuts" /></h3>
             <hr />
          
            <c:if test="${model.groupBloggingEnabled}">               
                <p>
                    <roller:link forward="createWebsite">
                       <fmt:message key="yourWebsites.createWeblog" />
                    </roller:link>
                    <br />
                    <fmt:message key="yourWebsites.createWeblog.desc" />
                </p>
            </c:if>
            
            <p>
                <roller:link forward="yourProfile">
                   <fmt:message key="yourWebsites.editProfile" />
                </roller:link>
                <br />
                <fmt:message key="yourWebsites.editProfile.desc" />
            </p>
            
            <p>
                <roller:link forward="rollerConfig">
                   <fmt:message key="yourWebsites.globalAdmin" />
                </roller:link>            
                <br />
                <fmt:message key="yourWebsites.globalAdmin.desc" />
            </p>
            
            <p>
                <roller:link forward="planetConfig">
                   <fmt:message key="yourWebsites.planetAdmin" />
                </roller:link>            
                <br />
                <fmt:message key="yourWebsites.planetAdmin.desc" />
            </p>
            
			<br />
			<br />
			<br />
			
            </div>
        </div>
    </div>
</div>	
