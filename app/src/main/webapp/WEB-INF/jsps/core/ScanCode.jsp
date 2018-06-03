<%--
    Copyright 2018 Glen Mazza

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

<div id="qr">
    <p><fmt:message key="scanCode.instructions"/></p>
    <img src="${qrCode}">
    <p>
        <fmt:message key="scanCode.relogin">
            <fmt:param><c:url value="/tb-ui/app/relogin"/></fmt:param>
        </fmt:message>
    </p>
</div>
