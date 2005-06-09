<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<br />
<h1><fmt:message key="pingTarget.confirmRemoveTitle" /></h1>

<p/>
<fmt:message key="pingTarget.confirmCommonRemove" />
<p/>

<table>
<tr><td><fmt:message key="pingTarget.name" />&nbsp;&nbsp;</td><td><b><c:out value="${pingTargetForm.name}" /></b></td></tr>
<tr><td><fmt:message key="pingTarget.pingUrl" />&nbsp;&nbsp;</td><td><b><c:out value="${pingTargetForm.pingUrl}" /></b></td></tr>
</table>

<table>
<tr>
<td>
<html:form action="/admin/commonPingTargets" method="post">
    <html:hidden property="method" value="deleteConfirmed" />
    <html:hidden property="id" />
    <div class="control">
       <input type="submit" value='<fmt:message key="pingTarget.removeOK" />' />
    </div>
</html:form>
</td>
<td>
<html:form action="/admin/commonPingTargets" method="post">
    <!-- Results in returning to the view on common ping targets. -->
    <div class="control">
       <input type="submit" value='<fmt:message key="pingTarget.cancel" />' />
    </div>
</html:form>
</td>
</tr>
</table>
<%@ include file="/theme/footer.jsp" %>
