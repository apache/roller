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

<h1><fmt:message key="weblogEntryImport.title" /></h1>

<roller:StatusMessage/>

<html:form action="/roller-ui/authoring/importEntries" method="post" focus="title">

    <html:hidden name="method" property="method" value="importEntries"/>

    <h3><fmt:message key="weblogEntryImport.selectXML" /></h3>

    <table cellspacing="0" cellpadding="0" class="edit">
        <tr>
            <td><fmt:message key="weblogEntryImport.XMLFile" /><br />
            <html:select property="importFileName" size="1" >
                <html:options property="xmlFiles" />
            </html:select>
            </td>
        </tr>

        <tr>
            <td class="buttonBox" colspan="1">
                <input type="button" name="post" value='<fmt:message key="weblogEntryImport.button.import" />'
                        onclick="submit()" />
            </td>
        </tr>
    </table>

</html:form>
