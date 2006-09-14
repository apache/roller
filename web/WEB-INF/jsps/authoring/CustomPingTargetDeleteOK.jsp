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
<%@ include file="/taglibs.jsp" %>

<p/>
<fmt:message key="pingTarget.confirmCustomRemove" />
<p/>

<table>
<tr><td><fmt:message key="pingTarget.name" />&nbsp;&nbsp;</td><td><b><c:out value="${pingTargetForm.name}" /></b></td></tr>
<tr><td><fmt:message key="pingTarget.pingUrl" />&nbsp;&nbsp;</td><td><b><c:out value="${pingTargetForm.pingUrl}" /></b></td></tr>
</table>

<table>
<tr>
<td>
<html:form action="/roller-ui/authoring/customPingTargets" method="post">
    <input type="hidden" name="weblog" value='<c:out value="${model.website.handle}" />' />
    <html:hidden property="method" value="deleteConfirmed" />
    <html:hidden property="id" />
    <div class="control">
       <input type="submit" value='<fmt:message key="pingTarget.removeOK" />' />
    </div>
</html:form>
</td>
<td>
<html:form action="/roller-ui/authoring/customPingTargets" method="post">
    <input type="hidden" name="weblog" value='<c:out value="${model.website.handle}" />' />
    <!-- Results in returning to the view on common ping targets. -->
    <div class="control">
       <input type="submit" value='<fmt:message key="pingTarget.cancel" />' />
    </div>
</html:form>
</td>
</tr>
</table>

