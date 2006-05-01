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

<%-- Provides a small login/logout form to include in a side bar. 

<div class="loginbox" align="center">
  <p>
  <hr />
  <wiki:UserCheck status="unvalidated">
    <form action="<wiki:Variable var="baseURL"/>Login.jsp" accept-charset="UTF-8" method="POST" >
    <p>
      <input type="hidden" name="page" value="<wiki:Variable var="pagename"/>" />
      <input type="text" name="uid" size="8" />
      <br />
      <input type="password" name="passwd" size="8" />
      <br />
      <input type="submit" name="action" value="login" />
    </p>
    </form>
  </wiki:UserCheck>
  <wiki:UserCheck status="validated">
    <form action="<wiki:Variable var="baseURL"/>Login.jsp" accept-charset="UTF-8">
    <p>
      <input type="hidden" name="page" value="<wiki:Variable var="pagename"/>" />
      <input type="submit" name="action" value="logout" />
    </p>
    </form>
  </wiki:UserCheck>
  </p>

</div>
--%>

