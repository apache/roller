
<%-- This page is designed to be included in edit-weblog.jsp --%>

<%@ include file="/taglibs.jsp" %>

<script type="text/javascript">
<!--
function postWeblogEntry(publish)
{
    if (publish)
        document.weblogEntryFormEx.publishEntry.value = "true";
    document.weblogEntryFormEx.submit();
}
// -->
</script>

<html:textarea property="text" cols="75" rows="20" style="width: 100%" tabindex="2"/>
