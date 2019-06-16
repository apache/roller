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

<%-- Success Message --%>
<c:if test='${actionMessage != null && !"".equals(actionMessage)}'>
    <div class="alert alert-success">
        <c:out value="${actionMessage}"/>
    </div>
</c:if>

<%-- Error Message --%>
<c:if test='${actionError != null && !"".equals(actionError)}'>
    <div class="alert alert-danger">
        <c:out value="${actionError}"/>
    </div>
</c:if>

<c:choose>
    <c:when test='${mfaEnabled}'>
        <p><fmt:message key="login.prompt.totp" /></p>
    </c:when>
    <c:otherwise>
        <p><fmt:message key="login.prompt" /></p>
    </c:otherwise>
</c:choose>

<form method="post" id="loginForm"
      action="<c:url value='/tb_j_security_check'/>">

    <sec:csrfInput/>

    <table width="80%">

        <tr>
            <td width="20%" align="right"><fmt:message key="login.userName" /></td>
            <td width="80%">
                <input type="text" name="username" id="j_username" size="25" onBlur="this.value=this.value.trim()"/>
            </td>
        </tr>

        <tr>
            <td width="20%" align="right"><fmt:message key="login.password" /></td>
            <td width="80%">
                <input type="password" name="password" id="j_password" size="20" onBlur="this.value=this.value.trim()"/>
            </td>
        </tr>

        <c:if test='${mfaEnabled}'>
            <tr>
                <td width="20%" align="right"><fmt:message key="login.totpCode" /></td>
                <td width="80%">
                    <input type="text" name="totpCode" size="20" onBlur="this.value=this.value.trim()"/>
                </td>
            </tr>
        </c:if>

        <tr>
            <td width="20%"></td>
            <td width="80%">
                <input type="submit" name="login" id="login" value="<fmt:message key='login.login' />" />
                <input type="reset" name="reset" id="reset" value="<fmt:message key='login.reset' />"
                    onclick="document.getElementById('j_username').focus()" />
            </td>
        </tr>

    </table>
</form>
