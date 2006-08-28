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
<%@ include file="/taglibs.jsp" %>
<%
try {
    Roller roller = RollerFactory.getRoller();
    pageContext.setAttribute("userCount", 
        new Integer(roller.getUserManager().getUsers(0,-1).size())); 
    pageContext.setAttribute("blogCount", 
        new Integer(roller.getUserManager().getWebsites(null, null, null, null, null, 0, -1).size()));
    pageContext.setAttribute("setupError", Boolean.FALSE);
} catch (Throwable t) {
    pageContext.setAttribute("setupError", Boolean.TRUE);
}
%>

<h1><fmt:message key="index.heading" /></h1>
<c:choose>
    <c:when test="${setupError}">
        <fmt:message key="index.error" />
    </c:when>
    <c:otherwise>
        <fmt:message key="index.prompt" /><br /><br />
        <div style="width:75%">
        <ul>
            <%-- 
                 Tell the user how to complete their Roller install, with helpful
                 notes and links to the appropriate places in the Roller UI.
            --%>

            <%-- STEP 1: Create a user if you don't already have one --%>
            <li><b><fmt:message key="index.createUser" />
                <c:if test="${userCount > 0}"> - 
                    <fmt:message key="index.createUserDone">
                        <fmt:param value="${userCount}" />
                    </fmt:message>
                </c:if>
                </b><br /><br />
                <fmt:message key="index.createUserHelp" /><br /><br />
                <fmt:message key="index.createUserBy" /> 
                <a href='<c:url value="/roller-ui/user.do?method=registerUser"/>'>
                    <fmt:message key="index.createUserPage" />
                </a>.
                <br /><br /><br />
            </li>

            <%-- STEP 2: Create a weblog if you don't already have one --%>
            <li><b><fmt:message key="index.createWeblog" />
                <c:if test="${blogCount > 0}"> - 
                    <fmt:message key="index.createWeblogDone">
                        <fmt:param value="${blogCount}" />
                    </fmt:message>
                </c:if>
                </b><br /><br />
                <fmt:message key="index.createWeblogHelp" /><br /><br />
                <fmt:message key="index.createWeblogBy" /> 
                <a href='<c:url value="/roller-ui/createWebsite.do?method=create"/>'>
                    <fmt:message key="index.createWeblogPage" />
                </a>
                <br /><br /><br />
            </li>

            <%-- STEP 3: Designate a weblog to be the frontpage weblot --%>
            <li><b><fmt:message key="index.setFrontpage" /></b><br />
                <br />
                <fmt:message key="index.setFrontpageHelp" /><br />
                <br />
                <fmt:message key="index.setFrontpageBy" /> 
                <a href='<c:url value="/roller-ui/admin/rollerConfig.do?method=edit"/>'>
                    <fmt:message key="index.setFrontpagePage" />
                </a>
            </li>

        </ul>
        </div>
    </c:otherwise>
</c:choose>

