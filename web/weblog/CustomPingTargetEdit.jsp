<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>
<script type="text/javascript">
// <!--
function cancel() {
    document.pingTargetForm.method.value="cancel"; 
    document.pingTargetForm.submit();
}
// -->
</script> 
<br />
<h1><fmt:message key="pingTarget.pingTarget" /></h1>

<br />
<html:form action="/editor/customPingTargets" method="post" focus="name">
    <html:hidden property="method" value="save" />
    <html:hidden property="id" />

    <div class="formrow">
       <label for="name" class="formrow"><fmt:message key="pingTarget.name" /></label>
       <html:text property="name" size="30" maxlength="30" />
    </div>

    <div class="formrow">
       <label for="pingUrl" class="formrow"><fmt:message key="pingTarget.pingUrl" /></label>
       <html:text property="pingUrl" size="45" maxlength="255" />
    </div>

    <p/>
    <div class="formrow">
       <label for="" class="formrow">&nbsp;</label>
       <input type="submit" value='<fmt:message key="pingTarget.save" />' />&nbsp;
       <input type="button" value='<fmt:message key="application.cancel" />' onclick="cancel()"></input>
    </div>

</html:form>

<%@ include file="/theme/footer.jsp" %>
