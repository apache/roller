
<%-- This page is designed to be included in edit-weblog.jsp --%>
<%@ page import="org.roller.presentation.weblog.formbeans.WeblogEntryFormEx" %>
<%@ include file="/taglibs.jsp" %>

<script type="text/javascript">
<!--
function postWeblogEntry(publish)
{
    document.weblogEntryFormEx.text.value = document.Ekit.getDocumentText();
    if (publish)
        document.weblogEntryFormEx.publishEntry.value = "true";
    document.weblogEntryFormEx.submit();
}
-->
</script>

<html:hidden property="text" />

<%-- Use the Ekit applet --%>

<%--
<%
String text = "";
WeblogEntryFormEx form 
    = (WeblogEntryFormEx) request.getAttribute("weblogEntryFormEx");
if (form.getText() != null) {
    text = form.getText();
}
String entryText = "<html><head></head><body>"+text+"</body></html>";
%>
<jsp:plugin type="applet" code="com.hexidec.ekit.EkitApplet.class" tabindex="2" 
    codebase="." jreversion="1.3" height="350" width="90%" name="Ekit"
    archive="ekitapplet.jar" type="application/x-java-applet;version=1.3"
    nspluginurl="http://java.sun.com/products/plugin/1.3.0_01/plugin-install.html" 
    iepluginurl="http://java.sun.com/products/plugin/1.3.0_01/jinstall-130_01-win32.cab#Version=1,3,0,1">
    <jsp:params>
        <jsp:param name="stylesheet" value="ekit.css"/>
        <jsp:param name="langcode" value="en" />
        <jsp:param name="langcountry" value="US" />
        <jsp:param name="toolbar" value="true"/>
        <jsp:param name="sourceview" value="false"/>
        <jsp:param name="exclusive" value="true"/>
        <jsp:param name="menuicons" value="true"/>
        <jsp:param name="scriptable" value="true"/>
        <jsp:param name="document" value="<%=entryText%>"/>
    </jsp:params>
    <jsp:fallback>
        <p>Unable to start Java Plugin.</p>
  </jsp:fallback>
</jsp:plugin>
--%>

<APPLET CODEBASE="." CODE="com.hexidec.ekit.EkitApplet.class" 
    ARCHIVE="ekitapplet.jar" NAME="Ekit" WIDTH="95%" HEIGHT="350">
<PARAM NAME="codebase" VALUE=".">
<PARAM NAME="code" VALUE="com.hexidec.ekit.EkitApplet.class">
<PARAM NAME="archive" VALUE="ekitapplet.jar">
<PARAM NAME="type" VALUE="application/x-java-applet;version=1.3">
<PARAM NAME="scriptable" VALUE="true">

<!-- Load text into Ekit applet by using the form bean -->
<PARAM NAME="DOCUMENT" 
    VALUE="<HTML><HEAD></HEAD><BODY><bean:write 
        name="weblogEntryFormEx" property="text" /></BODY></HTML>">

<PARAM NAME="STYLESHEET" VALUE="ekit.css">
<PARAM NAME="LANGCODE" VALUE="en">
<PARAM NAME="LANGCOUNTRY" VALUE="US">
<PARAM NAME="TOOLBAR" VALUE="true">
<PARAM NAME="SOURCEVIEW" VALUE="false">
<PARAM NAME="EXCLUSIVE" VALUE="true">
<PARAM NAME="MENUICONS" VALUE="true">
</APPLET>   


<br /><a href="http://www.hexidec.com">Ekit</a> 
editor by Howard Kistler of 
<a href="http://www.hexidec.com">hexidec codex</a>
<br />
<br />


