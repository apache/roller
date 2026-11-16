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

<div class="row">
    <div class="col-lg-8 offset-lg-2">

<s:if test="error">

        <h2><s:text name="installer.errorUpgradingTables"/></h2>
        <div class="alert alert-danger" role="alert">
            <s:text name="installer.errorUpgradingTablesExplanation"/>
        </div>
        <%@ include file="/WEB-INF/jsps/core/InstallMessages.jspf" %>

</s:if>
<s:elseif test="upgradeRequired">

        <h2><s:text name="installer.databaseUpgradeNeeded"/></h2>
        <p>
            <s:text name="installer.databaseUpgradeNeededExplanation">
                <s:param value="databaseProductName"/>
            </s:text>
        </p>
        <p><s:text name="installer.upgradeTables"/></p>

        <s:form action="install!upgrade" theme="simple">
            <s:hidden name="salt"/>
            <s:submit value="%{getText('installer.yesUpgradeTables')}" cssClass="btn btn-primary" />
        </s:form>

</s:elseif>
<s:else>

        <h2><s:text name="installer.tablesUpgraded"/></h2>
        <div class="alert alert-success" role="status">
            <s:text name="installer.tablesUpgradedExplanation"/>
        </div>

        <s:form action="install!bootstrap" theme="simple">
            <s:hidden name="salt"/>
            <s:submit value="%{getText('installer.finishUpgrade')}" cssClass="btn btn-primary" />
        </s:form>

        <%@ include file="/WEB-INF/jsps/core/InstallMessages.jspf" %>

</s:else>

    </div>
</div>
