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
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<s:if test="activationStatus == null">
    <p><s:text name="welcome.accountCreated" /></p>
    <p><a id="a_clickHere" href="<s:url action="login-redirect"/>" ><s:text name="welcome.clickHere" /></a>
    <s:text name="welcome.toLoginAndPost" /></p>
</s:if>

<s:elseif test="activationStatus == 'pending'">
    <p><s:text name="welcome.accountCreated" /></p>
    <p><s:text name="welcome.user.account.not.activated" /></p>
</s:elseif>

<s:elseif test="activationStatus == 'active'">
    <p><s:text name="welcome.user.account.activated" /></p>
    <p><a href="<s:url action="login-redirect"/>" ><s:text name="welcome.clickHere" /></a>
    <s:text name="welcome.toLoginAndPost" /></p>
</s:elseif>

<br />
<br />
<br />
