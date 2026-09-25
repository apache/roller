<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<div class="row">
    <div class="col-sm-8 offset-sm-2 col-md-6 offset-md-3">
        <h2><s:text name="installer.bootstrap.heading" /></h2>
        <p><s:text name="installer.bootstrap.instructions" /></p>

        <s:form action="bootstrap-token!redeem" method="post" theme="simple">
            <s:hidden name="salt" />
            <div class="mb-3">
                <label class="form-label" for="setup-token"><s:text name="installer.bootstrap.tokenLabel" /></label>
                <s:textfield id="setup-token" name="token" theme="simple"
                        cssClass="form-control" autocomplete="off" autofocus="autofocus"
                        required="required"
                        oninput="document.getElementById('setup-token-submit').disabled = !this.value.trim()" />
                <p class="form-text"><s:text name="installer.bootstrap.tokenHelp" /></p>
            </div>
            <div class="mb-3">
                <s:submit id="setup-token-submit" value="%{getText('installer.bootstrap.submit')}"
                        theme="simple" disabled="true"
                        cssClass="btn btn-primary" />
            </div>
        </s:form>
    </div>
</div>
