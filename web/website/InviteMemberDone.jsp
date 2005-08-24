<%@ include file="/taglibs.jsp" %>

<h1><fmt:message key="inviteMemberDone.title" /></h1>

<p>
<fmt:message key="inviteMemberDone.message" >
    <fmt:param value="${user.userName}" />
</fmt:message>
</p>

<img src="../images/ComposeMail16.gif" alt="mail-icon" align="bottom" />
<roller:link page="/editor/inviteMember.do">
   <fmt:message key="inviteMemberDone.inviteAnother" />
</roller:link>
                
<br />




