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

<%-- Media pages use the sidebar for folders, so they show the user and weblog in one line
     instead of the "Powered by" card --%>

<p class="roller-context-line">
    <s:if test="authenticatedUser != null">
        <a href="<s:url action="menu" namespace="/roller-ui" />"><s:property value="authenticatedUser.userName"/></a>
    </s:if>
    <s:if test="authenticatedUser != null && actionWeblog != null"><span aria-hidden="true">&nbsp;&middot;&nbsp;</span></s:if>
    <s:if test="actionWeblog != null">
        <s:text name="mainPage.currentWebsite" />:
        <a href='<s:property value="actionWeblog.absoluteURL" />'><s:property value="actionWeblog.handle" /></a>
    </s:if>
</p>
