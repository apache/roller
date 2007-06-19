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
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<script type="text/javascript">
<!--
function editorCleanup() {
    // no-op
}
function changeSize(e, num) {
    a = e.rows + num;
    if (a > 0) e.rows = a;
    var expires = new Date();
    expires.setTime(expires.getTime() + 24 * 90 * 60 * 60 * 1000); // sets it for approx 90 days.
    setCookie("editorSize",e.rows,expires);
}
function changeSize1(e, num) {
    a = e.rows + num;
    if (a > 0) e.rows = a;
    var expires = new Date();
    expires.setTime(expires.getTime() + 24 * 90 * 60 * 60 * 1000); // sets it for approx 90 days.
    setCookie("editorSize1",e.rows,expires);
}
// -->
</script>

<%-- ===================================================================== --%>
<p class="toplabel"><s:text name="weblogEdit.content" /></p>

<s:textarea name="bean.text" cols="75" rows="25" cssStyle="width: 100%" tabindex="5"/>
<script type="text/javascript">
    <!--
    if (getCookie("editorSize1") != null) {
        document.getElementById('entry_bean_text').rows = getCookie("editorSize1");
    }
    -->
</script>
<table style="width:100%"><tr><td align="right">
  <!-- Add buttons to make this textarea taller or shorter -->
  <input type="button" name="taller" value=" &darr; " onclick="changeSize1(document.getElementById('entry_bean_text'), 5)" />
  <input type="button" name="shorter" value=" &uarr; " onclick="changeSize1(document.getElementById('entry_bean_text'), -5)" />
</td></tr></table>

<%-- ===================================================================== --%>
<p class="toplabel"><s:text name="weblogEdit.summary" /></p>

<s:textarea name="bean.summary" cols="75" rows="5" cssStyle="width: 100%" tabindex="6"/>
<script type="text/javascript">
    <!--
    if (getCookie("editorSize") != null) {
        document.getElementById('entry_bean_summary').rows = getCookie("editorSize");
    }
    -->
</script>
<table style="width:100%"><tr><td align="right">
  <!-- Add buttons to make this textarea taller or shorter -->
  <input type="button" name="taller" value=" &darr; " onclick="changeSize(document.getElementById('entry_bean_summary'), 5)" />
  <input type="button" name="shorter" value=" &uarr; " onclick="changeSize(document.getElementById('entry_bean_summary'), -5)" />
</td></tr></table>
