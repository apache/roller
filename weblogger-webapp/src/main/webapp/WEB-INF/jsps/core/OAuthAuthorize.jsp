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

<p class="subtitle">
   <s:text name="oauthAuthorize.description" >
       <s:param value="authenticatedUser.userName" />
   </s:text>
</p>

<p class="pagetip">
   <s:text name="oauthAuthorize.tip" >
       <s:param value="appDesc" />
   </s:text>
</p>

<form name="authZForm" action="authorize" method="POST">
    <input type="hidden" name="userId" value="<s:property value="userName" />" size="20" /><br>
    <input type="hidden" name="oauth_token" value='<s:property value="token" />' />
    <input type="hidden" name="oauth_callback" value='<s:property value="callback" />' />
    <input type="submit" name="Authorize" value="Authorize"/>
</form>
    
</body>
</html>
