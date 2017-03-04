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
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>

<div class="bannerStatusBox">

    <table class="bannerStatusBox" cellpadding="0" cellspacing="0">
        <tr>
            <td class="bannerLeft">

                <c:if test="${authenticatedUser != null}">
                    <fmt:message key="mainPage.loggedInAs" /> <c:out value="${authenticatedUser.userName}"/>
                </c:if>


                <c:if test="${actionWeblog != null}">
                    - <fmt:message key="mainPage.currentWebsite" />
                    <b><a href='<c:out value="${actionWeblog.absoluteURL}" />'>
                            <c:out value="${actionWeblog.handle}" />
                    </a></b>
                </c:if>

            </td>

            <td class="bannerRight">

                <c:if test="${authenticatedUser == null}">
                   <a href="<c:url value='/'/>"><fmt:message key="navigationBar.homePage" /></a> |
                </c:if>

                <c:if test="${userIsAdmin}">
                    <a href="<c:url value='/tb-ui/app/admin/globalConfig'/>"><fmt:message key="mainMenu.globalAdmin" /></a> |
                </c:if>

                <c:choose>
                    <c:when test="${authenticatedUser != null}">
                       <a href="<c:url value='/tb-ui/app/home'/>"><fmt:message key="mainMenu.title" /></a> |
                       <a href="<c:url value='/tb-ui/app/profile'/>"><fmt:message key="mainMenu.editProfile" /></a> |
                       <a href="<c:url value='/tb-ui/app/logout'/>"><fmt:message key="navigationBar.logout"/></a>
                    </c:when>

                    <c:otherwise>
                        <a href="<c:url value='/tb-ui/app/login-redirect'/>"><fmt:message key="navigationBar.login"/></a>

                        <c:if test="${registrationPolicy != 'DISABLED' && authenticationMethod != 'LDAP'}">
                            | <a href="<c:url value='/tb-ui/app/register'/>"><fmt:message key="navigationBar.register"/></a>
                        </c:if>
                    </c:otherwise>
                </c:choose>

            </td>
        </tr>
    </table>

</div>
