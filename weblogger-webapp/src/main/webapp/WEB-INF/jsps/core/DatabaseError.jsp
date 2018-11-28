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
    [<s:property value="getRootCauseException().getClass().getName()" />]
</p>

<p><s:text name="installer.heresTheStackTrace" /></p>
<pre>
    [<s:property value="getRootCauseStackTrace()" />]
</pre>

<br />
<br />
