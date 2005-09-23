<%@ include file="/taglibs.jsp" %>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            
             <h3><fmt:message key="yourWebsites.actions" /></h3>
             <hr size="1" noshade="noshade" />
          
            <c:if test="${model.groupBloggingEnabled}">               
                <h3>
                <roller:link forward="createWebsite">
                   <fmt:message key="yourWebsites.createWeblog" />
                </roller:link>
                </h3>
                <p><fmt:message key="yourWebsites.createWeblog.desc" /></p>
            </c:if>
            
            <h3>
            <roller:link forward="yourProfile">
               <fmt:message key="yourWebsites.editProfile" />
            </roller:link>
            </h3>
            <p><fmt:message key="yourWebsites.editProfile.desc" /></p>
            
            <c:if test="${model.rollerSession.globalAdminUser}">               
                <h3>
                <roller:link forward="rollerConfig">
                   <fmt:message key="yourWebsites.globalAdmin" />
                </roller:link> 
                </h3>          
                <p><fmt:message key="yourWebsites.globalAdmin.desc" /></p>

                <c:if test="${model.planetAggregatorEnabled}">               
                    <h3>
                    <roller:link forward="planetConfig">
                       <fmt:message key="yourWebsites.planetAdmin" />
                    </roller:link>            
                    </h3>
                    <p><fmt:message key="yourWebsites.planetAdmin.desc" /></p>
                </c:if>
            </c:if>
            
			<br />
			
        </div>
    </div>
</div>	
