<%--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<h2><s:text name="installer.cannotConnectToDatabase" /></h2>

<h3><s:text name="installer.whatHappened" /></h3>

<p><s:text name="installer.whatHappenedDatabaseConnectionError" /></p>
<ul>
   <s:iterator value="messages">
      <li><s:property/></li>
   </s:iterator>
</ul>
    
<h3><s:text name="installer.whyDidThatHappen" /></h3>

<p>
    <s:text name="installer.aboutTheException" />
    [<s:property value="rootCauseExceptionName" />]
</p>

<p><s:text name="installer.heresTheStackTrace" /></p>
<pre>
    [<s:property value="getRootCauseStackTrace()" />]
</pre>

<br />
<br />
