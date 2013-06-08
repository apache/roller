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
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<p class="subtitle">
    <s:text name="weblogExport.subtitle" >
        <s:param value="actionWeblog.handle" />
    </s:text>
</p>

<p class="pagetip">
    <s:text name="weblogExport.tip" />
</p>

<h2><s:text name="weblogExport.entries" /></h2>
<p class="pagetip" style="width:50%"><s:text name="weblogExport.entries.tip" /></p>
<s:form name="entriesExport" action="weblogExport!exportEntries" method="POST">
	<s:hidden name="salt" />
    <table cellpadding="0" cellspacing="5" border="0">
        <tr>
            <td valign="baseline" align="right"><s:text name="weblogExport.baseUrl"/></td>
            <td valign="baseline"><s:textfield name="baseUrl" size="30" /></td>
        </tr>
        <tr>
            <td valign="baseline" align="right"><s:text name="weblogExport.format" /></td>
            <td valign="baseline"><s:radio theme="roller" name="format" list="formatOptions" value="format"/></td>
        </tr>
        <tr>
            <td valign="baseline" colspan="2">
                <s:submit key="weblogExport.exportEntries" />
                <s:hidden name="weblog" />
            </td>
        </tr>
    </table>
</s:form>

<h2><s:text name="weblogExport.resources" /></h2>
<p class="pagetip" style="width:50%"><s:text name="weblogExport.resources.tip" /></p>
<s:form name="resourcesExport" action="weblogExport!exportResources" method="POST">
	<s:hidden name="salt" />
    <table cellpadding="0" cellspacing="5" border="0">
        <tr>
            <td valign="baseline">
                <s:submit key="weblogExport.exportResources" />
                <s:hidden name="weblog" />
            </td>
        </tr>
    </table>
</s:form>
