
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
<APPLET CODEBASE="../" CODE="com.hexidec.ekit.EkitApplet.class"
    ARCHIVE="ekitappletspell.jar" NAME="Ekit" WIDTH="95%" HEIGHT="550">
<PARAM NAME="codebase" VALUE="../editor">
<PARAM NAME="code" VALUE="com.hexidec.ekit.EkitApplet.class">
<PARAM NAME="archive" VALUE="ekitappletspell.jar">
<PARAM NAME="type" VALUE="application/x-java-applet;version=1.3">
<PARAM NAME="scriptable" VALUE="true">

<!-- Load text into Ekit applet by using the form bean -->
<PARAM NAME="DOCUMENT"
    VALUE="<HTML><HEAD></HEAD><BODY><bean:write
        name="weblogEntryFormEx" property="text" /></BODY></HTML>">
        
<PARAM NAME="BASE64" VALUE="false">
<PARAM NAME="STYLESHEET" VALUE="ekit.css">
<PARAM NAME="LANGCODE" VALUE="en">
<PARAM NAME="LANGCOUNTRY" VALUE="US">
<PARAM NAME="TOOLBAR" VALUE="true">
<PARAM NAME="TOOLBARMULTI" VALUE="true">
<PARAM NAME="SOURCEVIEW" VALUE="false">
<PARAM NAME="EXCLUSIVE" VALUE="true">
<PARAM NAME="SPELLCHECK" VALUE="true">
<PARAM NAME="MENUICONS" VALUE="true">
<PARAM NAME="MENU_EDIT" VALUE="true">
<PARAM NAME="MENU_VIEW" VALUE="true">
<PARAM NAME="MENU_FONT" VALUE="true">
<PARAM NAME="MENU_FORMAT" VALUE="true">
<PARAM NAME="MENU_INSERT" VALUE="true">
<PARAM NAME="MENU_TABLE" VALUE="true">
<PARAM NAME="MENU_FORMS" VALUE="true">
<PARAM NAME="MENU_SEARCH" VALUE="true">
<PARAM NAME="MENU_TOOLS" VALUE="true">
<PARAM NAME="MENU_HELP" VALUE="true">
</APPLET>  


<br /><a href="http://www.hexidec.com">Ekit</a> 
editor by Howard Kistler of 
<a href="http://www.hexidec.com">hexidec codex</a>
<br />
<br />


