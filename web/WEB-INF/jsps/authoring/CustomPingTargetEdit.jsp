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
<script type="text/javascript">
// <!--
function cancel() {
    document.pingTargetForm.method.value="cancel"; 
    document.pingTargetForm.submit();
}
// -->
</script> 

<p class="subtitle">
<fmt:message key="customPingTarget.subtitle">
    <fmt:param value="${model.website.handle}" />
</fmt:message>
</p>

<html:form action="/roller-ui/authoring/customPingTargets" method="post" focus="name">
    <html:hidden property="method" value="save" />
    <html:hidden property="id" />
    <input type="hidden" name="weblog" value='<c:out value="${model.website.handle}" />' />

    <div class="formrow">
       <label for="name" class="formrow"><fmt:message key="pingTarget.name" /></label>
       <html:text property="name" size="30" maxlength="30" />
    </div>

    <div class="formrow">
       <label for="pingUrl" class="formrow"><fmt:message key="pingTarget.pingUrl" /></label>
       <html:text property="pingUrl" size="45" maxlength="255" />
    </div>

    <p/>
    <div class="formrow">
       <label for="" class="formrow">&nbsp;</label>
       <input type="submit" value='<fmt:message key="pingTarget.save" />' />&nbsp;
       <input type="button" value='<fmt:message key="application.cancel" />' onclick="cancel()"></input>
    </div>

</html:form>

