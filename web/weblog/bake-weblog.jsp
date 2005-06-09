<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp"%>

<div id="content" style="margin-top: 40px">

<roller:StatusMessage/>
<html:errors />

<h3>Bake your Weblog Template</h3>

    <html:form action="/editor/bake" method="post">
        <input type="submit" value="Bake" />
        <input type=hidden name="method" value="bake" />
    </html:form>
</div>

<%@ include file="/theme/footer.jsp"%>