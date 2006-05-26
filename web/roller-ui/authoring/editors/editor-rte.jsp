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
<%@ include file="/taglibs.jsp" %>
<html:hidden property="summary" />
<html:hidden property="text" />

<script type="text/javascript" src="<%= request.getContextPath() %>/roller-ui/authoring/editors/rte/richtext.js" ></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/roller-ui/authoring/editors/rte/html2xhtml.js" ></script>
<script type="text/javascript">
<!--
    function postWeblogEntry(publish) {
	    updateRTE('rte1');
        document.weblogEntryFormEx.summary.value = document.weblogEntryFormEx.rte1.value;
	    updateRTE('rte2');
        document.weblogEntryFormEx.text.value = document.weblogEntryFormEx.rte2.value;
        if (publish) document.weblogEntryFormEx.publishEntry.value = "true";
        document.weblogEntryFormEx.submit();
    }
   // Usage: initRTE(imagesPath, includesPath, cssFile, genXHTML)
   initRTE("<%= request.getContextPath() %>/roller-ui/authoring/editors/rte/images/", "<%= request.getContextPath() %>/roller-ui/authoring/editors/rte/", '', false);
//-->
</script>
<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>

<%-- ===================================================================== --%>
<p class="toplabel"><fmt:message key="weblogEdit.summary" /></p>

<script language="JavaScript" type="text/javascript">
<!--
// Usage: writeRichText(fieldname, html, width, height, buttons, readOnly)
writeRichText('rte1', document.weblogEntryFormEx.summary.value, '100%', '50px', true, false);
//-->
</script>


<script type="text/javascript">
<!--
if (getCookie("rte1size") != null) {
    var frame = document.getElementById('rte1');
    frame.height = getCookie("rte1size");
}
-->
</script>     

<!--
<div style="float:right">
<input type="button" name="taller" value=" &darr; " onclick="changeSize('rte1', 'rte1size', 20)" />
<input type="button" name="shorter" value=" &uarr; " onclick="changeSize('rte1', 'rte1size', -20)" />
</div>
-->

<%-- ===================================================================== --%>
<p class="toplabel"><fmt:message key="weblogEdit.content" /></p>

<script language="JavaScript" type="text/javascript">
<!--
// Usage: writeRichText(fieldname, html, width, height, buttons, readOnly)
writeRichText('rte2', document.weblogEntryFormEx.text.value, '100%', '250px', true, false);
//-->
</script>


<script type="text/javascript">
<!--
if (getCookie("rte2size") != null) {
    var frame = document.getElementById('rte2');
    frame.height = getCookie("rte2size");
}
-->
</script>     

<!-- Add buttons to make this textarea taller or shorter -->
<!-- 
<input type="button" name="taller" value=" &darr; " onclick="changeSize('rte2', 'rte2size', 20)" />
<input type="button" name="shorter" value=" &uarr; " onclick="changeSize('rte2', 'rte2size', -20)" />
-->


