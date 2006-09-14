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
RollerContext rctx = RollerContext.getRollerContext();
RollerSession rollerSession = RollerSession.getRollerSession(request);
UserData user = rollerSession.getAuthenticatedUser();

WebsiteData website = null;
String handle = request.getParameter(RequestConstants.WEBLOG);
if (handle != null) {
    Roller roller = RollerFactory.getRoller();
    website = roller.getUserManager().getWebsiteByHandle(handle);
}
String absURL = RollerRuntimeConfig.getAbsoluteContextURL();
boolean allowNewUsers = RollerRuntimeConfig.getBooleanProperty("users.registration.enabled");
String customRegUrl = RollerRuntimeConfig.getProperty("users.registration.url");
if(customRegUrl != null && customRegUrl.trim().equals(""))
    customRegUrl = null;
%>
<div class="bannerStatusBox">
    
    <table class="bannerStatusBox" cellpadding="0" cellspacing="0">
    <tr>
    <td class="bannerLeft">
    
        <% if (user != null) { %>
            <fmt:message key="mainPage.loggedInAs" />
            <html:link forward="yourProfile">
               <%= user.getUserName() %>
            </html:link>
        <% } %>
        
        <c:if test="${!empty model.website}" >
           - <fmt:message key="mainPage.currentWebsite" />
           <b><a href='<c:out value="${model.website.URL}" />'>
               <c:out value="${model.website.handle}" />
           </a></b>
   
        </c:if>
        
    </td>

    <td class="bannerRight">
            
        <roller:link forward="main">
            <%= RollerRuntimeConfig.getProperty("site.shortName") %>
        </roller:link>

        | <roller:link forward="yourWebsites">
            <fmt:message key="mainPage.mainMenu" />
        </roller:link>

        <% if (user != null) { %>
            | <html:link forward="logout-redirect">
                <fmt:message key="navigationBar.logout"/>
            </html:link>
        <% } else { %>
            | <html:link forward="login-redirect">
                <fmt:message key="navigationBar.login"/>
            </html:link>
            
            <% if(allowNewUsers) { %>
                | <html:link forward="registerUser">
                    <fmt:message key="navigationBar.register"/>
                </html:link>
            <% } else if(customRegUrl != null) { %>
                | <a href="<%= customRegUrl %>">
                    <fmt:message key="navigationBar.register"/>
                </a>
            <% } %>
            
        <% } %>

    </td>
    </tr>
    </table>
    
</div>


