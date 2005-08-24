<%@ include file="/taglibs.jsp" %>


<br />
<html:form action="/admin/commonPingTargets" method="post" focus="name">
    <html:hidden property="method" value="save" />
    <html:hidden property="id" />

    <div class="formrow">
       <label for="name" class="formrow"><fmt:message key="pingTarget.name" /></label>
       <html:text property="name" size="30" maxlength="30" />
    </div>

    <div class="formrow">
       <label for="pingUrl" class="formrow"><fmt:message key="pingTarget.pingUrl" /></label>
       <html:text property="pingUrl" size="100" maxlength="255" />
    </div>

    <p/>
    <div class="formrow">
       <label for="" class="formrow">&nbsp;</label>
       <input type="submit" value='<fmt:message key="pingTarget.save" />' />
    </div>

</html:form>


