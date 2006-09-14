<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.
-->
<%@ include file="/taglibs.jsp" %>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            
<div class="sidebarInner">
             <h3><fmt:message key="yourWebsites.actions" /></h3>
             <hr size="1" noshade="noshade" />
          
            <c:if test="${model.groupBloggingEnabled || (empty model.permissions && empty model.pendings)}">               
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
</div>	
