<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<%-- Form allows deleting of referers --%>
<h1><fmt:message key="referers.todaysReferers" /></h1>
<p class="subtitle">
    <fmt:message key="referers.subtitle" >
        <fmt:param value="${model.rollerSession.currentWebsite.handle}" />
    </fmt:message>
</p>  
<p class="pagetip">
    <fmt:message key="referers.tip" />
</p>

<html:form action="/editor/referers" method="post">
<input type="hidden" name="method" value="delete" />

<%-- Table of referers, with check box for each --%>
<table width="75%" class="rollertable" >
    <tr class="rollertable">
        <th class="rollertable"></th>
        <th class="rollertable"><fmt:message key="referers.url" /></th>
        <th class="rollertable"><fmt:message key="referers.hits" /></th>
    </tr>
 
    <c:forEach var="referer" items="${referers}">
        <tr>
            <td class="rollertable">
                <input type="checkbox" name="id" value='<c:out value="${referer.id}" />' />
            </td>
            <td class="rollertable">               
               <c:out value="${referer.displayUrl}" escapeXml="false" />
            </td>
            <td class="rollertable"><c:out value="${referer.dayHits}" /></td>
        </tr>
    </c:forEach> 
    
</table>

<br />
<input type="submit" value='<fmt:message key="referers.deleteSelected" />' /></input>
</html:form>

<br />

<%-- Form allows reset of day hits --%>
<h1><fmt:message key="referers.hitCounters" /></h1>
<p><fmt:message key="referers.hits" />: <c:out value="${pageHits}"/></p>
<html:form action="/editor/referers" method="post">
    <input type="hidden" name="method" value="reset" />
    <input type="submit" value='<fmt:message key="referers.reset" />' /></input>
</html:form>

<%@ include file="/theme/footer.jsp" %>
