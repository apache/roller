<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.
-->
<%-- This page is designed to be included in edit-weblog.jsp --%>
<%@ page import="org.apache.roller.presentation.weblog.formbeans.WeblogEntryFormEx" %>
<%@ include file="/taglibs.jsp" %>
<html:hidden property="summary" />

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
<APPLET CODEBASE="../editor/" CODE="com.hexidec.ekit.EkitApplet.class"
    ARCHIVE="ekitappletspell.jar" NAME="Ekit" WIDTH="95%" HEIGHT="550">
<PARAM NAME="codebase" VALUE="../editor/">
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


