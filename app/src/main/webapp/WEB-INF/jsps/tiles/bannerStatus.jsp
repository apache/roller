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

<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container-fluid">
        <a class="navbar-brand" href="#"><s:property value="%{getProp('site.name')}" /></a>
        <button type="button" class="navbar-toggler" data-bs-toggle="collapse"
                data-bs-target="#navbar" aria-expanded="false" aria-controls="navbar"
                aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div id="navbar" class="collapse navbar-collapse">

            <ul class="navbar-nav">

                <s:if test="actionWeblog != null">
                    
                    <s:set var="tabMenu" value="menu"/>
                    <s:if test="#tabMenu != null">

                        <s:iterator var="tab" value="#tabMenu.tabs">
                            <li class="nav-item dropdown">
                                <a href="#" class="nav-link dropdown-toggle" data-bs-toggle="dropdown" role="button"
                                   aria-expanded="false">
                                    <s:text name="%{#tab.key}"/>
                                </a>
                                <ul class="dropdown-menu">
                                    <s:iterator var="tabItem" value="#tab.items" status="stat">
                                        <li>
                                            <a class="dropdown-item" href="<s:url action="%{#tabItem.action}">
                                                <s:param name="weblog" value="actionWeblog.handle"/></s:url>">
                                                <s:text name="%{#tabItem.key}"/>
                                            </a>
                                        </li>
                                    </s:iterator>
                                </ul>
                            </li>
                        </s:iterator>

                    </s:if>
                    
                </s:if>

                <s:if test="actionWeblog == null">

                    <s:set var="tabMenu" value="menu"/>
                    <s:if test="#tabMenu != null">

                        <s:iterator var="tab" value="#tabMenu.tabs">
                            <li class="nav-item dropdown">
                                <a href="#" class="nav-link dropdown-toggle" data-bs-toggle="dropdown" role="button"
                                   aria-expanded="false">
                                    <s:text name="%{#tab.key}"/>
                                </a>
                                <ul class="dropdown-menu">
                                    <s:iterator var="tabItem" value="#tab.items" status="stat">
                                        <li>
                                            <a class="dropdown-item" href="<s:url action='%{#tabItem.action}' />">
                                                <s:text name="%{#tabItem.key}"/>
                                            </a>
                                        </li>
                                    </s:iterator>
                                </ul>
                            </li>
                        </s:iterator>

                    </s:if>

                </s:if>

            </ul>
            
            <ul class="navbar-nav ms-auto">
                
                <li class="nav-item"><a class="nav-link" href="<s:url value='/'/>"><s:property value="getProp('site.shortName')"/></a></li>

                <li class="nav-item">
                    <a class="nav-link" href="<s:url action='menu' namespace='/roller-ui' />">
                        <s:text name="mainPage.mainMenu" /></a>
                </li>

                <s:if test="authenticatedUser != null">
                    <li class="nav-item">
                        <a class="nav-link" href="<s:url action='logout' namespace='/roller-ui' />">
                            <s:text name="navigationBar.logout"/></a>
                    </li>
                </s:if>
                <s:else>
                    <li class="nav-item">
                        <a class="nav-link" href="<s:url action='login-redirect' namespace='/roller-ui' />">
                            <s:text name="navigationBar.login"/></a>
                    </li>

                    <s:if test="getBooleanProp('users.registration.enabled') && getProp('authentication.method') != 'ldap'">
                        <li class="nav-item">
                            <a class="nav-link" href="<s:url action='register' namespace='/roller-ui' />">
                                <s:text name="navigationBar.register"/></a>
                        </li>
                    </s:if>
                    
                    <s:elseif test="getProp('users.registration.url') != null && getProp('users.registration.url') > 0">
                        <li>
                            <a href="<s:property value="getProp('users.registration.url')"/>">
                                <s:text name="navigationBar.register"/></a>
                        </li>
                    </s:elseif>
                </s:else>
                
            </ul>
        </div><!--/.nav-collapse -->
    </div>
</nav>


