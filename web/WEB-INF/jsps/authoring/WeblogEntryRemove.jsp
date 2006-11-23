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
<%@ include file="/WEB-INF/jsps/taglibs.jsp" %>

<h2>
<jsp:useBean id="weblogEntryFormEx"  scope="session"
    class="org.apache.roller.ui.authoring.struts.formbeans.WeblogEntryFormEx"/>
<fmt:message key="weblogEntryRemove.removeWeblogEntry" /> [<jsp:getProperty name="weblogEntryFormEx" property="title"/>]
</h2>

<p><fmt:message key="weblogEntryRemove.areYouSure" /></p>
<p>
<fmt:message key="weblogEntryRemove.entryTitle" /> = [<jsp:getProperty name="weblogEntryFormEx" property="title"/>]<br />
<fmt:message key="weblogEntryRemove.entryId" /> = [<jsp:getProperty name="weblogEntryFormEx" property="id"/>]
</p>

<table>
    <tr>
        <td>
            <html:form action="/roller-ui/authoring/weblog" method="post">
                <input type="submit" value='<fmt:message key="weblogEntryRemove.yes" />' /></input>
                <html:hidden property="method" value="remove"/></input>
                <html:hidden property="id" /></input>
            </html:form>
        </div>
        </td>
        <td>
            <html:form action="/roller-ui/authoring/weblog" method="post">
                <input type="submit" value='<fmt:message key="weblogEntryRemove.no" />' /></input>
                <html:hidden property="method" value="cancel"/></input>
                <html:hidden property="id" /></input>
            </html:form>
        </td>
    </tr>
</table>

