<%@ include file="/taglibs.jsp" %>

<c:set var="status_code" value="${requestScope['javax.servlet.error.status_code']}" />
<c:set var="message"     value="${requestScope['javax.servlet.error.message']}" />
<c:set var="type"        value="${requestScope['javax.servlet.error.type']}" />
<c:set var="exception"   value="${requestScope['javax.servlet.error.exception']}" />
    
<h2 class="error"><fmt:message key="errorPage.title" /></h2>

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
    <td><c:out value="${exception}" /></td>
</tr>
</table>

<c:if test="${!empty exception}">
    <form>
        <textarea rows="30" style="font-size:8pt;width:80%">
        <% 
        java.io.StringWriter sw = new java.io.StringWriter();
        Throwable t = (Throwable)request.getAttribute("exception");
        t.printStackTrace(new java.io.PrintWriter(sw));
        %>
        <%= sw.toString() %>
        </textarea>
    </form>
</c:if>

<br />
<br />







