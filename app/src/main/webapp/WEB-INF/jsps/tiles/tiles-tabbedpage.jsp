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
<!doctype html>
<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/favicon.ico">
        <title>TightBlog: <c:out value="${pageTitle}" /></title>
        <tiles:insertAttribute name="head" />
    </head>
    <c:set var="useAngular"><tiles:getAsString name="useAngularHeader"/></c:set>
    <body <c:if test="${useAngular == 'true'}">
          id='ngapp-div' ng-app='tightblogApp' ng-controller='PageController as ctrl' </c:if>>
        <div id="banner">
            <tiles:insertAttribute name="bannerStatus" />
        </div>
        
        <div id="menu">
            <h1><c:out value="${pageTitle}" /></h1>
            <tiles:insertAttribute name="menu" />
        </div>
        
        <div id="content"> 
            <div id="<tiles:insertAttribute name='sidebarType'/>_maincontent_wrap">
                <div id="maincontent">
                    <tiles:insertAttribute name="messages" /> 
                    <tiles:insertAttribute name="content" />    
                </div>
            </div>
            
            <div id="<tiles:insertAttribute name='sidebarType'/>_rightcontent_wrap">
                <div id="rightcontent"> 
                    <tiles:insertAttribute name="sidebar" ignore="true" />
                </div>
            </div>
        </div>
        
        <div id="footer">
            <tiles:insertAttribute name="footer" />
        </div>
        
    </body>
</html>
