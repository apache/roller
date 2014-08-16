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

<script>
<%@ include file="/roller-ui/scripts/ajax-user.js" %>
</script>

<p class="subtitle"><s:text name="userAdmin.subtitle.searchUser" /></p>
<p><s:text name="userAdmin.prompt.searchUser" /></p>

<s:form action="modifyUser" method="GET">
   	<s:hidden name="salt" />
 
    <span style="margin:4px"><s:text name="inviteMember.userName" /></span>
    <input name="bean.userName" id="userName" size="30" maxlength="30"
           onfocus="onUserNameFocus(null)" onkeyup="onUserNameChange(null)" 
           style="margin:4px" />
    <input type="submit" value='<s:text name="generic.edit" />'
           style="margin:4px" />
    <br />
    <select id="userList" size="10" onchange="onUserSelected()" 
            style="width:400px; margin:4px" ></select>
    
</s:form>

<%-- LDAP uses external user creation --%>
<s:if test="authMethod != 'LDAP'">
    <p class="subtitle"><s:text name="userAdmin.subtitle.userCreation" /></p>
    <s:text name="userAdmin.prompt.orYouCan" />
    <s:url action="createUser" id="createUser" />
    <a href="<s:property value="createUser" />">
        <s:text name="userAdmin.prompt.createANewUser" />
    </a>
</s:if>

<%-- this forces focus to the userName field --%>
<script>
    document.getElementById('userName').focus();
</script>
