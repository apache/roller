<%--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<h3><s:text name="mainPage.actions" /></h3>
<hr size="1" noshade="noshade" />

<span class="bi bi-plus-lg"></span>
<s:url action="invite" var="inviteUrl">
   <s:param name="weblog" value="%{actionWeblog.handle}" />
</s:url>
<a href='<s:property value="#inviteUrl" />'>
    <s:text name="memberPermissions.inviteMember" />
</a>
<s:text name="memberPermissions.whyInvite" />

<h3> <s:text name="memberPermissions.permissionsHelpTitle" /> </h3>
<hr size="1" noshade="noshade" />

<s:text name="memberPermissions.permissionHelp" />
