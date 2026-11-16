<%--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<p>
    <s:text name="yourWebsites.confirmResignation">
        <s:param value="weblog"/>
    </s:text>
</p>

<s:form action="memberResign!resign"  cssClass="form-horizontal">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
    <s:submit theme="simple" cssClass="btn btn-danger" value="%{getText('generic.yes')}"/>&nbsp;
    <s:submit theme="simple" cssClass="btn btn-outline-secondary" value="%{getText('generic.no')}" action="menu"/>
</s:form>
