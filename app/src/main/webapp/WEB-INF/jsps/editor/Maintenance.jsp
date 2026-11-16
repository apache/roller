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

<p class="subtitle"><s:text name="maintenance.subtitle" /></p>
    
<s:form action="maintenance" cssClass="form-vertical">
	<s:hidden name="salt" />
    <s:hidden name="weblog" value="%{actionWeblog.handle}" />

    <p><s:text name="maintenance.prompt.flush" /></p>
    <s:submit theme="simple" value="%{getText('maintenance.button.flush')}" action="maintenance!flushCache" cssClass="btn btn-outline-secondary" />

    <s:if test="getBooleanProp('search.enabled')">
        <p><s:text name="maintenance.prompt.index" /></p>
        <s:submit theme="simple" value="%{getText('maintenance.button.index')}" action="maintenance!index" cssClass="btn btn-outline-secondary" />
    </s:if>

    <p><s:text name="maintenance.prompt.reset" /></p>
    <s:submit theme="simple" value="%{getText('maintenance.button.reset')}" action="maintenance!reset" cssClass="btn btn-outline-secondary" />

</s:form>
