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

<c:if test="${status.error}">
    <h2><fmt:message key="installer.startupProblemMessage"/></h2>

    <h3><fmt:message key="installer.whatHappened"/></h3>
</c:if>
<c:choose>
    <c:when test="${status.name() == 'databaseError'}">
        <p><fmt:message key="installer.databaseConnectionError"/></p>
        <ul>
            <c:forEach var="message" items="${messages}">
                <c:out value="${message}"/>
            </c:forEach>
        </ul>
    </c:when>
    <c:when test="${status.name() == 'databaseVersionError'}">
        <p><fmt:message key="installer.databaseVersionError"/></p>
    </c:when>
    <c:when test="${status.name() == 'tablesMissing'}">
        <h2><fmt:message key="installer.noDatabaseTablesFound"/></h2>

        <p>
            <fmt:message key="installer.noDatabaseTablesExplanation">
                <fmt:param value="${databaseProductName}"/>
            </fmt:message>
        </p>
        <p><fmt:message key="installer.createTables"/></p>

        <s:form action="install!create">
            <sec:csrfInput/>
            <s:submit value="%{getText('installer.yesCreateTables')}"/>
        </s:form>
    </c:when>
    <c:when test="${status.name() == 'databaseCreateError'}">
        <p><fmt:message key="installer.databaseCreateError"/></p>
        <pre>
            <c:forEach var="message" items="${messages}">
                <c:out value="${message}"/>
            </c:forEach>
        </pre>
    </c:when>
    <c:when test="${status.name() == 'needsBootstrapping'}">
        <h2><fmt:message key="installer.tablesCreated"/></h2>

        <p><fmt:message key="installer.tablesCreatedExplanation"/></p>
        <p>
            <fmt:message key="installer.tryBootstrapping">
                <fmt:param><c:url value="/tb-ui/install.rol!bootstrap"/></fmt:param>
            </fmt:message>
        </p>
        <pre>
            <c:forEach var="message" items="${messages}">
                <c:out value="${message}"/>
            </c:forEach>
        </pre>
    </c:when>
    <c:when test="${status.name() == 'bootstrapError'}">
        <p><fmt:message key="installer.bootstrappingError"/></p>
    </c:when>
</c:choose>

<c:if test="${rootCauseStackTrace != null && rootCauseStackTrace != ''}">
    <h3><fmt:message key="installer.whyDidThatHappen"/></h3>
    <p><fmt:message key="installer.heresTheStackTrace"/></p>
    <pre>
        [<c:out value="${rootCauseStackTrace}"/>]
    </pre>
</c:if>
<br/>
<br/>
