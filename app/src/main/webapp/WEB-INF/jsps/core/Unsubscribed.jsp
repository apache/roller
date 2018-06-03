<%--
    Copyright 2017 Glen Mazza

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>

<c:choose>
    <c:when test="${weblogEntryTitle != null}">
        <h2>
            <fmt:message key="unsubscribed.title">
                <fmt:param><c:url value="${weblogEntryTitle}"/></fmt:param>
            </fmt:message>
        </h2>

        <c:choose>
            <c:when test="${found == true}">
                <fmt:message key="unsubscribed.success"/>
            </c:when>
            <c:otherwise>
                <fmt:message key="unsubscribed.failure"/>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
        <fmt:message key="unsubscribed.error"/>
    </c:otherwise>
</c:choose>
