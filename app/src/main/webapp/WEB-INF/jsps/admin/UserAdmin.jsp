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

<script>
<%@ include file="/roller-ui/scripts/ajax-user.js" %>
</script>

<p class="subtitle">
<b><s:text name="userAdmin.subtitle.searchUser" /></b>
<s:text name="userAdmin.prompt.searchUser" />
</p>

<s:form action="userAdmin!edit" method="POST" theme="bootstrap" cssClass="form-vertical">
   	<s:hidden name="salt" />

    <s:textfield cssClass="form-control"
        id="userName"
        name="bean.userName"
        label="%{getText('inviteMember.userName')}"
        onfocus="onUserNameFocus(null)"
        onkeyup="onUserNameChange(null)" />

    <s:select class="form-control" id="userList" size="10" onchange="onUserSelected()" list="bean.list" />

    <button type="submit" class="btn btn-outline-secondary" id="user-submit">
        <s:text name="generic.edit" />
    </button>

</s:form>

<s:if test="authMethod != 'LDAP'"> <%-- if we're not doing LDAP we can create new users in Roller --%>

    <h3><s:text name="userAdmin.subtitle.userCreation" /></h3>
    <s:text name="userAdmin.prompt.orYouCan" />
    <s:url action="createUser" var="createUser" />
    <a href="<s:property value="createUser" />">
        <s:text name="userAdmin.prompt.createANewUser" />
    </a>

</s:if>

<script>


$(document).ready(function () {

    document.getElementById('userName').focus();
    onUserNameFocus(false);

});

</script>
