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

<s:if test="status.error">
<h2><s:text name="installer.startupProblemMessage" /></h2>

<h3><s:text name="installer.whatHappened" /></h3>
</s:if>
<s:if test="status.name() == 'databaseError'">
    <p><s:text name="installer.databaseConnectionError" /></p>
    <ul>
       <s:iterator value="messages">
          <li><s:property/></li>
       </s:iterator>
    </ul>
</s:if>
<s:elseif test="status.name() == 'databaseVersionError'">
    <p><s:text name="installer.databaseVersionError" /></p>
</s:elseif>
<s:elseif test="status.name() == 'tablesMissing'">
    <h2><s:text name="installer.noDatabaseTablesFound" /></h2>

    <p>
        <s:text name="installer.noDatabaseTablesExplanation">
            <s:param value="databaseProductName" />
        </s:text>
    </p>
    <p><s:text name="installer.createTables" /></p>

    <s:form action="install!create">
        <sec:csrfInput/>
        <s:submit value="%{getText('installer.yesCreateTables')}" />
    </s:form>
</s:elseif>
<s:elseif test="status.name() == 'databaseCreateError'">
    <p><s:text name="installer.databaseCreateError" /></p>
    <pre>
        <s:iterator value="messages"><s:property/><br></s:iterator>
    </pre>
</s:elseif>
<s:elseif test="status.name() == 'needsBootstrapping'">
    <h2><s:text name="installer.tablesCreated" /></h2>

    <p><s:text name="installer.tablesCreatedExplanation" /></p>
    <p>
        <s:text name="installer.tryBootstrapping">
            <s:param><s:url action="install!bootstrap"/></s:param>
        </s:text>
    </p>
    <pre>
        <s:iterator value="messages"><s:property/><br /></s:iterator>
    </pre>
</s:elseif>
<s:elseif test="status.name() == 'bootstrapError'">
    <p><s:text name="installer.bootstrappingError" /></p>
</s:elseif>

<s:if test="rootCauseStackTrace != null && rootCauseStackTrace != ''">
    <h3><s:text name="installer.whyDidThatHappen" /></h3>
    <p><s:text name="installer.heresTheStackTrace" /></p>
    <pre>
        [<s:property value="rootCauseStackTrace" />]
    </pre>
</s:if>
<br />
<br />
