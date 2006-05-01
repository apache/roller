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
--%><% response.setContentType("text/html; charset=UTF-8"); %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/taglibs.jsp" %><html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%= RollerRuntimeConfig.getProperty("site.shortName") %>: <c:out value="${model.title}" /></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<tiles:insert attribute="head" />     
<style type="text/css">
<tiles:insert attribute="styles" />
</style>
</head>
<body>

<div id="banner">
    <tiles:insert attribute="banner" />
    <tiles:insert attribute="bannerStatus" />
</div>
    
<div id="wrapper"> 
    <div id="leftcontent_wrap">
        <div id="leftcontent"> 
        </div>
    </div>
    
    <div id="centercontent_wrap">
        <div id="centercontent">   
            <h1><c:out value="${model.title}" /></h1>
            <tiles:insert attribute="messages" /> 
            <tiles:insert attribute="content" />    
        </div>
    </div>
    
    <div id="rightcontent_wrap">
        <div id="rightcontent"> 
        </div>
    </div>
</div>

<div id="footer">
    <tiles:insert attribute="footer" />
</div>

<div id="datetagdiv" 
   style="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;">
</div>
</body>
</html>
