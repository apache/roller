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


<s:if test="error">

    <h2><s:text name="installer.errorUpgradingTables"/></h2>
    <p><s:text name="installer.errorUpgradingTablesExplanation"/></p>
    <pre>
        <s:iterator value="messages"><s:property/><br/></s:iterator>
    </pre>

</s:if>
<s:elseif test="upgradeRequired">

    <h2><s:text name="installer.databaseUpgradeNeeded"/></h2>

    <p>
        <s:text name="installer.databaseUpgradeNeededExplanation">
            <s:param value="databaseProductName"/>
        </s:text>
    </p>
    <p><s:text name="installer.upgradeTables"/></p>

    <s:form action="install!upgrade" class="form-horizontal">
        <s:hidden name="salt"/>
        <s:submit value="%{getText('installer.yesUpgradeTables')}" cssClass="btn btn-primary" />
    </s:form>

</s:elseif>
<s:else>

    <h2><s:text name="installer.tablesUpgraded"/></h2>

    <p><s:text name="installer.tablesUpgradedExplanation"/></p>
    <%-- the message's link submits this form; bootstrapping needs a POST --%>
    <p>
        <s:text name="installer.tryBootstrapping">
            <s:param>#bootstrap</s:param>
        </s:text>
    </p>
    <s:url var="bootstrapUrl" action="install!bootstrap" />
    <form id="bootstrapForm" action="<s:property value="#bootstrapUrl" />" method="post">
        <input type="hidden" name="salt" value="<s:property value="salt" />" />
        <noscript><button type="submit"><s:text name="installer.finishUpgrade" /></button></noscript>
    </form>
    <script>
        document.querySelector('a[href="#bootstrap"]').addEventListener('click', function (event) {
            event.preventDefault();
            document.getElementById('bootstrapForm').submit();
        });
    </script>

    <pre>
        <s:iterator value="messages"><s:property/><br/></s:iterator>
    </pre>

    <s:form action="install!bootstrap" class="form-horizontal">
        <s:hidden name="salt"/>
        <s:submit value="%{getText('installer.finishUpgrade')}" cssClass="btn btn-primary" />
    </s:form>

</s:else>
