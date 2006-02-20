<%@ include file="/taglibs.jsp" %>

<h2 class="error"><fmt:message key="error.title.403" /></h2>

<c:set var="status_code" value="${requestScope['javax.servlet.error.status_code']}" />
<c:set var="message"     value="${requestScope['javax.servlet.error.message']}" />
<c:set var="type"        value="${requestScope['javax.servlet.error.type']}" />

<table width="80%" border="1px" style="border-collapse: collapse;">
<tr>
    <td width="20%">Status Code</td>
    <td><c:out value="${status_code}" /></td>
</tr>
<tr>
    <td width="20%">Message</td>
    <td><c:out value="${message}" /></td>
</tr>
<tr>
    <td width="20%">Type</td>
    <td><c:out value="${type}" /></td>
</tr>
<tr>
    <td width="20%">Exception</td>
    <td><fmt:message key="error.text.403" /></td>
</tr>
</table>

<br />
<br />



