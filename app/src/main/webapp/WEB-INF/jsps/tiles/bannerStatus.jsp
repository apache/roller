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

<div class="bannerStatusBox">

    <table class="bannerStatusBox" cellpadding="0" cellspacing="0">
        <tr>
            <td class="bannerLeft">

                <s:if test="authenticatedUser != null">
                    <s:text name="mainPage.loggedInAs" /> <s:property value="authenticatedUser.userName"/>
                </s:if>


                <s:if test="actionWeblog != null">
                    - <s:text name="mainPage.currentWebsite" />
                    <b><a href='<s:property value="actionWeblog.absoluteURL" />'>
                            <s:property value="%{actionWeblog.handle}" />
                    </a></b>
                </s:if>

            </td>

            <td class="bannerRight">

                <s:if test="authenticatedUser == null">
                   <a href="<s:url value='/'/>"><s:property value="getProp('site.name')"/></a> |
                </s:if>

                <s:if test="userIsAdmin">
                    <a href="<s:url action='globalConfig' namespace='/tb-ui/admin' />"><s:text name="yourWebsites.globalAdmin" /></a> |
                </s:if>

                <s:if test="authenticatedUser != null">
                   <a href="<s:url action='menu' namespace='/tb-ui' />"><s:text name="yourWebsites.title" /></a> |
                   <a href="<s:url action='profile' namespace='/tb-ui' />"><s:text name="yourWebsites.editProfile" /></a> |
                   <a href="<s:url action='logout' namespace='/tb-ui' />"><s:text name="navigationBar.logout"/></a>
                </s:if>

                <s:else>
                    <a href="<s:url action='login-redirect' namespace='/tb-ui' />"><s:text name="navigationBar.login"/></a>

                    <s:if test="getProp('user.registration.process') != 'DISABLED' && getProp('authentication.method') != 'ldap'">
                        | <a href="<s:url action='register' namespace='/tb-ui' />"><s:text name="navigationBar.register"/></a>
                    </s:if>
                </s:else>

            </td>
        </tr>
    </table>

</div>
