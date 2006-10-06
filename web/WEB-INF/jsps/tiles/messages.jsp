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
<%@ include file="/taglibs.jsp" %>
<script type="text/javascript">
<!--
function dirty() {
    messages = document.getElementById("messages");
    if (messages != null) {
	    messages.className = "warnings";
	    var n1 = messages.childNodes[0];
	    var n2 = document.createTextNode("Unsaved changes");
	    messages.replaceChild(n2, n1);
    }
}
-->
</script>

<%-- Success Messages --%>
<logic:messagesPresent message="true">
    <div id="messages" class="messages">
        <html:messages id="message" message="true">
            <c:out value="${message}" escapeXml="false"/><br />
        </html:messages>
    </div>
</logic:messagesPresent>

<%-- Error Messages --%>
<logic:messagesPresent>
    <div id="errors" class="errors">
        <html:messages id="error">
            <c:out value="${error}" /><br />
        </html:messages>
    </div>
</logic:messagesPresent>
