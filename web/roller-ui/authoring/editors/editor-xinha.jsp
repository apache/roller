<%--
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
--%>
<%-- This page is designed to be included in edit-weblog.jsp --%>
<%@ include file="/WEB-INF/jsps/taglibs.jsp" %>

<script type="text/javascript">
<!--
function postWeblogEntry() {
    document.weblogEntryFormEx.text.value =    xinha_editors.xe_content.getHTML().trim();
    document.weblogEntryFormEx.summary.value = xinha_editors.xe_summary.getHTML().trim();
    document.weblogEntryFormEx.submit();
}

// (preferably absolute) URL (including trailing slash) where Xinha is installed
_editor_url  = '<c:url value="/roller-ui/authoring/editors/xinha/" />' 

// And the language we need to use in the editor.
_editor_lang = "en"; 
-->
</script>
<script type="text/javascript" src='<c:url value="/roller-ui/authoring/editors/xinha/htmlarea.js" />'></script>
<script type="text/javascript" src='<c:url value="/roller-ui/authoring/editors/xinha/my_config.js" />'></script> 
<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>

<%-- ===================================================================== --%>
<b><fmt:message key="weblogEdit.content" /></b><br />
<html:textarea styleId="xe_content" property="text" rows="25" cols="50" style="width: 100%"></html:textarea>

<%-- ===================================================================== --%>
<b><fmt:message key="weblogEdit.summary" /></b><br />
<html:textarea styleId="xe_summary" property="summary" rows="10" cols="50" style="width: 100%"></html:textarea>


 


