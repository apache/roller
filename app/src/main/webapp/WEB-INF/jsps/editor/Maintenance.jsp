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

<p class="subtitle"><s:text name="maintenance.subtitle" /></p>
    
<s:form action="maintenance">
	<s:hidden name="salt" />
    <s:hidden name="weblog" value="%{actionWeblog.handle}" />

    <s:text name="maintenance.prompt.flush" /><br /><br />
    <s:submit value="%{getText('maintenance.button.flush')}" action="maintenance!flushCache" />

    <s:if test="getBooleanProp('search.enabled')">
        <br /><br />
        <s:text name="maintenance.prompt.index" /><br /><br />
        <s:submit value="%{getText('maintenance.button.index')}" action="maintenance!index" />	
    </s:if>

    <br /><br />
    <s:text name="maintenance.prompt.reset" /><br /><br />
    <s:submit value="%{getText('maintenance.button.reset')}" action="maintenance!reset" />

</s:form>
