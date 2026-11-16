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

<h3><s:text name="pagesForm.addNewPage"/></h3>
<hr size="1" noshade="noshade"/>

<s:form action="templates!add" theme="bootstrap" id="templateAdd" cssClass="form-horizontal">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>

    <s:text name="generic.name"/>
    <s:textfield name="newTmplName"/>

    <s:if test="!availableActions.isEmpty">
        <s:text name="pagesForm.action"/>
        <s:select name="newTmplAction" list="availableActions"/>
    </s:if>

    <s:submit theme="simple" cssClass="btn btn-outline-secondary" value="%{getText('pagesForm.add')}"/>

</s:form>

