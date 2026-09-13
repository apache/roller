<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
<h2>Roller initial setup</h2>
<p>Enter the one-time setup token printed in the Roller server log.</p>
<s:form action="bootstrap-token!redeem" method="post">
    <s:textfield name="token" label="Setup token" autocomplete="off" />
    <s:submit value="Continue" />
</s:form>
