<%--
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
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            
        <div class="sidebarInner">

            <h3><s:text name="mainPage.actions" /></h3>            
            <hr size="1" noshade="noshade" />
            
            <img src='<s:url value="/images/email_edit.png"/>' alt="mail-icon" align="bottom" />
            <s:url action="invite" id="inviteUrl">
               <s:param name="weblog" value="%{actionWeblog.handle}" />
            </s:url>
            <a href='<s:property value="#inviteUrl" />'>
                <s:text name="memberPermissions.inviteMember" />
            </a>
            <br />
            <s:text name="memberPermissions.whyInvite" />       
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
                <s:text name="memberPermissions.permissionsHelpTitle" />
            </h3>
            <hr size="1" noshade="noshade" />
            
            <s:text name="memberPermissions.permissionHelp" />	
		    <br />
		    <br />
        </div>		    
            </div>
        </div>
    </div>
</div>	
