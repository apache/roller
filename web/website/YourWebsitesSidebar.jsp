<%@ include file="/taglibs.jsp" %>

<div class="sidebarfade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">
            
             <h3><fmt:message key="yourWebsites.shortCuts" /></h3>
             <hr />
          
            <c:if test="${model.groupBloggingEnabled}">               
                <p>
                    <h3>
                    <roller:link forward="createWebsite">
                       <fmt:message key="yourWebsites.createWeblog" />
                    </roller:link>
                    </h3>
                    <fmt:message key="yourWebsites.createWeblog.desc" />
                </p>
            </c:if>
            
            <p>
                <h3>
                <roller:link forward="yourProfile">
                   <fmt:message key="yourWebsites.editProfile" />
                </roller:link>
                </h3>
                <fmt:message key="yourWebsites.editProfile.desc" />
            </p>
            
            <p>
                <h3>
                <roller:link forward="rollerConfig">
                   <fmt:message key="yourWebsites.globalAdmin" />
                </roller:link> 
                </h3>          
                <fmt:message key="yourWebsites.globalAdmin.desc" />
            </p>
            
            <p>
                <h3>
                <roller:link forward="planetConfig">
                   <fmt:message key="yourWebsites.planetAdmin" />
                </roller:link>            
                </h3>
                <fmt:message key="yourWebsites.planetAdmin.desc" />
            </p>
            
			<br />
			
            </div>
        </div>
    </div>
</div>	
