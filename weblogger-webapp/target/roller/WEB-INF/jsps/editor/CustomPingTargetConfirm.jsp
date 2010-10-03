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

<br />
<h1><s:text name="" /></h1>

<p/>
<s:text name="pingTarget.confirmCustomRemove" />
<p/>

<table>
    <tr><td><s:text name="pingTarget.name" />&nbsp;&nbsp;</td><td><b><s:property value="pingTarget.name" /></b></td></tr>
    <tr><td><s:text name="pingTarget.pingUrl" />&nbsp;&nbsp;</td><td><b><s:property value="pingTarget.pingUrl" /></b></td></tr>
</table>

<br/>

<div class="control">
    <s:form action="customPingTargets!delete">
        <s:hidden name="pingTargetId" />
        <s:hidden name="weblog" value="%{actionWeblog.handle}" />
        <s:submit value="%{getText('pingTarget.removeOK')}" />
    </s:form>
    &nbsp;
    <s:form action="customPingTargets">
        <s:hidden name="weblog" value="%{actionWeblog.handle}" />
        <s:submit value="%{getText('pingTarget.cancel')}" />
    </s:form>
</div>
