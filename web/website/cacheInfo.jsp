<%@ include file="/taglibs.jsp" %>

<p class="subtitle"><fmt:message key="cacheInfo.subtitle" /></a>
<p><fmt:message key="cacheInfo.prompt" /></a>

<c:forEach var="cache" items="${cacheStats}">
    <c:if test="${!empty cache.value}">
        <table cellspacing="3" border="1">
            <tr>
                <th colspan="2"><c:out value="${cache.key}"/></th>
            </tr>

            <c:forEach var="prop" items="${cache.value}">
                <tr>
                    <td><c:out value="${prop.key}"/></td>
                    <td><c:out value="${prop.value}"/></td>
                </tr>
            </c:forEach>

            <tr>
                <td colspan="2">
                    <form action="cacheInfo.do" method="POST">
                        <input type="hidden" name="cache" value="<c:out value='${cache.key}'/>">
                        <input type="submit" name="method" value="clear">
                    </form>
                </td>
            </tr>
            
        </table>
        
        <br>
    </c:if>
</c:forEach>