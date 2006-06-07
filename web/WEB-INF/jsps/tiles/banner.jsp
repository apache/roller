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
String theme = request.getParameter("look");
if (theme == null) theme = RollerConfig.getProperty("editor.theme");
String logourl = "/roller-ui/theme/" + theme + "/logo.gif";
request.setAttribute("logourl", logourl);
%>
<!--
<div class="bannerBox">
    <div id="logoshadow">
       <div id="logoimage">
       </div>
    </div>
</div>
-->

<div class="bannerBox">

<div id="logo">
    <!-- Transparent PNG fix for IE, thanks to Kenneth M. Kolano -->
    <div id="logoshadow" 
        style="_background-image:none; filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='<c:out value='${model.baseURL}' />/roller-ui/images/logo-shadow.png',sizingMethod='crop');">
        <div id="logobackground">
            <a href='<c:out value="${model.baseURL}" />/main.do' id="logoimage" 
                style="_background-image:none; filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='<c:out value='${model.baseURL}' />/roller-ui/images/transparent-logo.png', sizingMethod='crop');">
            </a>
        </div>
    </div>
</div>

</div>