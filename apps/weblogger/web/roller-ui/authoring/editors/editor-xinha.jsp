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
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<script type="text/javascript">
function editorCleanup() {
    $('xe_content').value = xinha_editors.xe_content.getHTML().trim();
    $('xe_summary').value = xinha_editors.xe_summary.getHTML().trim();
}

// (preferably absolute) URL (including trailing slash) where Xinha is installed
_editor_url  = '<s:url value="/roller-ui/authoring/editors/xinha/" />' 

// And the language we need to use in the editor.
_editor_lang = "en"; 
</script>
<script type="text/javascript" src='<s:url value="/roller-ui/authoring/editors/xinha/htmlarea.js" />'></script>
<script type="text/javascript" src='<s:url value="/roller-ui/authoring/editors/xinha/my_config.js" />'></script> 
<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>

<%-- ===================================================================== --%>
<b><s:text name="weblogEdit.content" /></b><br />
<s:textarea id="xe_content" name="bean.text" rows="25" cols="50" cssStyle="width: 100%" />

<%-- ===================================================================== --%>
<b><s:text name="weblogEdit.summary" /></b><br />
<s:textarea id="xe_summary" name="bean.summary" rows="10" cols="50" cssStyle="width: 100%" />
 
