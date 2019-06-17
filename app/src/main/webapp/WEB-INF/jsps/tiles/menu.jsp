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

<s:set var="tabMenu" value="menu"/>
<s:if test="#tabMenu != null">

    <%--
    <nav class="navbar navbar-default">
        <div class="container-fluid">
            <div id="navbar" class="navbar-collapse collapse">
                <ul class="nav navbar-nav">

                    <s:iterator id="tab" value="#tabMenu.tabs">
                        <li class="dropdown">
                            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" 
                                aria-haspopup="true" aria-expanded="false">
                                <s:text name="%{#tab.key}"/> <span class="caret"></span>
                            </a>
                            <ul class="dropdown-menu">
                                <s:iterator id="tabItem" value="#tab.items" status="stat">
                                    <li>
                                        <a href="<s:url action="%{#tabItem.action}"><s:param name="weblog" value="actionWeblog.handle"/></s:url>">
                                            <s:text name="%{#tabItem.key}"/>
                                        </a>
                                    </li>
                                </s:iterator>
                            </ul>
                        </li>
                    </s:iterator>
                    
                </ul>
            </div> <!--/.nav-collapse -->
        </div> <!--/.container-fluid -->
    </nav>

    <s:iterator id="tab" value="#tabMenu.tabs">

        <h3><s:text name="%{#tab.key}"/></h3>

        <div class="list-group">
            <s:iterator id="tabItem" value="#tab.items" status="stat">
                <a class="list-group-item" href="<s:url action="%{#tabItem.action}"><s:param name="weblog" value="actionWeblog.handle"/></s:url>">
                    <s:text name="%{#tabItem.key}"/></a>
            </s:iterator>
        </div>

    </s:iterator>
    --%>
        
</s:if>
