<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<h1><fmt:message key="inviteMemberDone.title" /></h1>

<p>
<fmt:message key="inviteMemberDone.message" >
    <fmt:param value="${user.userName}" />
</fmt:message>
</p>

<roller:link page="/editor/inviteMember.do">
   <fmt:message key="inviteMemberDone.inviteAnother" />
</roller:link>
                
<br />

<%@ include file="/theme/footer.jsp" %>


