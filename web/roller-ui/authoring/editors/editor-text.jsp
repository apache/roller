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
function postWeblogEntry(publish) {
    if (publish)
        document.weblogEntryFormEx.publishEntry.value = "true";
    document.weblogEntryFormEx.submit();
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
<p class="toplabel"><fmt:message key="weblogEdit.content" /></p>

<html:textarea property="text" cols="75" rows="25" style="width: 100%" tabindex="5"/>
<script type="text/javascript">
    <!--
    if (getCookie("editorSize1") != null) {
        document.weblogEntryFormEx.text.rows = getCookie("editorSize1");
    }
    -->
</script>
<table style="width:100%"><tr><td align="right">
  <!-- Add buttons to make this textarea taller or shorter -->
  <input type="button" name="taller" value=" &darr; " onclick="changeSize1(document.weblogEntryFormEx.text, 5)" />
  <input type="button" name="shorter" value=" &uarr; " onclick="changeSize1(document.weblogEntryFormEx.text, -5)" />
</td></tr></table>

<%-- ===================================================================== --%>
<p class="toplabel"><fmt:message key="weblogEdit.summary" /></p>

<html:textarea property="summary" cols="75" rows="5" style="width: 100%" tabindex="6"/>
<script type="text/javascript">
    <!--
    if (getCookie("editorSize") != null) {
        document.weblogEntryFormEx.summary.rows = getCookie("editorSize");
    }
    -->
</script>
<table style="width:100%"><tr><td align="right">
  <!-- Add buttons to make this textarea taller or shorter -->
  <input type="button" name="taller" value=" &darr; " onclick="changeSize(document.weblogEntryFormEx.summary, 5)" />
  <input type="button" name="shorter" value=" &uarr; " onclick="changeSize(document.weblogEntryFormEx.summary, -5)" />
</td></tr></table>
