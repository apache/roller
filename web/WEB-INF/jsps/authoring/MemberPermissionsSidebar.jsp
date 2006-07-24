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

            <h3><fmt:message key="mainPage.actions" /></h3>            
            <hr size="1" noshade="noshade" />
            
            <img src='<c:url value="/images/email_edit.png"/>' alt="mail-icon" align="bottom" />
            <c:url value="/roller-ui/authoring/inviteMember.do" var="inviteUrl">
               <c:param name="weblog" value="${model.website.handle}" />
            </c:url>
            <a href='<c:out value="${inviteUrl}" />'>
                <fmt:message key="memberPermissions.inviteMember" />
            </a>
            <br />
            <fmt:message key="memberPermissions.whyInvite" />       
            <br />
            <br />
            
            <img src='<c:url value="/images/email_edit.png"/>' alt="mail-icon" align="bottom" />
            <c:url value="/roller-ui/authoring/invitations.do" var="invitationsUrl">
               <c:param name="weblog" value="${model.website.handle}" />
            </c:url>
            <a href='<c:out value="${invitationsUrl}" />'>
                <fmt:message key="memberPermissions.manageInvitations" />
            </a>
            <br />
            <fmt:message key="memberPermissions.whyManageInvitations" />       
            <br />
            <br />
            
        </div>                					
        </div>
    </div>
</div>	

<br />
<br />

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">
            <div class="sidebarInner">
            
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
</div>	


