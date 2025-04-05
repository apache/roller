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
<!doctype html>
<html>
    <head>
      <meta charset="utf-8">
      <meta http-equiv="X-UA-Compatible" content="IE=edge">
      <link rel="icon" href="<%= request.getContextPath() %>/favicon.svg" type="image/x-icon">
      <title><s:text name="error" /></title>
      <tiles:insertAttribute name="head" />
      <style>
          <tiles:insertAttribute name="styles" />
      </style>
    </head>
    <body>
        
        <div id="banner">
            <tiles:insertAttribute name="banner" />
        </div>
        
        <div id="wrapper" class="container-fluid">
            <div id="leftcontent_wrap">
                <div id="leftcontent"> 
                </div>
            </div>
            
            <div id="centercontent_wrap">
                <div id="centercontent">
                    <tiles:insertAttribute name="messages" />
                    <tiles:insertAttribute name="content" />    
                </div>
            </div>
            
            <div id="rightcontent_wrap">
                <div id="rightcontent"> 
                </div>
            </div>
        </div>
        
        <div id="footer">
            <tiles:insertAttribute name="footer" />
        </div>
        
    </body>
</html>
