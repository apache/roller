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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>

<%-- Body of the login page, invoked from login.jsp --%>
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<s:if test="activationStatus == 'active'">
    <div class="messages">
        <p><s:text name="welcome.user.account.activated" /></p>
    </div>
</s:if>

<s:if test="activationStatus == 'activePending'">
    <div class="messages">
        <p><s:text name="welcome.user.account.need.approval" /></p>
    </div>
</s:if>

<p><s:text name="loginPage.prompt" /></p>

<form method="post" id="loginForm"
      action="<s:url value='/roller_j_security_check'/>">

    <sec:csrfInput/>

    <table width="80%">

        <tr>
            <td width="20%" align="right"><s:text name="loginPage.userName" /></td>
            <td width="80%">
                <input value="gmazza" type="text" name="username" id="j_username" size="25" onBlur="this.value=this.value.trim()"/>
            </td>
        </tr>

        <tr>
            <td width="20%" align="right"><s:text name="loginPage.password" /></td>
            <td width="80%">
                <input value="1@Password" type="password" name="password" id="j_password" size="20" onBlur="this.value=this.value.trim()"/>
            </td>
        </tr>

        <tr>
            <td width="20%"></td>
            <td width="80%">
                <input type="submit" name="login" id="login" value="<s:text name='loginPage.login' />" />
                <input type="reset" name="reset" id="reset" value="<s:text name='loginPage.reset' />"
                    onclick="document.getElementById('j_username').focus()" />
            </td>
        </tr>

    </table>
</form>
