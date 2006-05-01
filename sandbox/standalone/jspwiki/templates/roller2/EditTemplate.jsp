<!--
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
-->
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
        "http://www.w3.org/TR/html4/loose.dtd">

<html>

<head>
  <title><wiki:Variable var="ApplicationName" /> Edit: <wiki:PageName /></title>
  <meta name="ROBOTS" content="NOINDEX">
  <%@ include file="cssinclude.js" %>
</head>

<wiki:CheckRequestContext context="edit">
  <body class="edit" bgcolor="#D9E8FF" onLoad="document.forms[1].text.focus()">
</wiki:CheckRequestContext>

<wiki:CheckRequestContext context="comment">
  <body class="comment" bgcolor="#EEEEEE" onLoad="document.forms[1].text.focus()">
</wiki:CheckRequestContext>

<div id="header">
</div>

<div id="left">
       <%@ include file="LeftMenu.jsp" %>
       <p>
       <wiki:LinkTo page="TextFormattingRules">Help on editing</wiki:LinkTo>
       </p>
       <%@ include file="LeftMenuFooter.jsp" %>
</div>

<div id="content">
      <wiki:CheckRequestContext context="comment">
         <wiki:Include page="CommentContent.jsp" />
      </wiki:CheckRequestContext>

      <wiki:CheckRequestContext context="edit">
         <wiki:Include page="EditContent.jsp" />
      </wiki:CheckRequestContext>
</div>

</body>

</html>
