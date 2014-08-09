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

<%-- Titling, processing actions different between add and edit --%>
<s:if test="actionName == 'commonPingTargetEdit'">
    <s:set var="mainAction">commonPingTargetEdit</s:set>
    <s:set var="subtitleKey">pingTargetEdit.subtitle</s:set>
</s:if>
<s:else>
    <s:set var="mainAction">commonPingTargetAdd</s:set>
    <s:set var="subtitleKey">pingTargetAdd.subtitle</s:set>
</s:else>

<p class="subtitle">
<s:text name="%{#subtitleKey}"/>
</p>

<s:form>
	<s:hidden name="salt" />
    <s:if test="actionName == 'commonPingTargetEdit'">
        <%-- bean for add does not have a bean id yet --%>
        <s:hidden name="bean.id" />
    </s:if>

    <div class="formrow">
       <label for="name" class="formrow"><s:text name="generic.name" /></label>
       <s:textfield name="bean.name" size="30" maxlength="30" style="width:50%"/>
    </div>

    <div class="formrow">
       <label for="pingUrl" class="formrow"><s:text name="pingTarget.pingUrl" /></label>
       <s:textfield name="bean.pingUrl" size="100" maxlength="255" style="width:50%"/>
    </div>

    <p/>

    <div class="formrow">
       <label for="" class="formrow">&nbsp;</label>
       <s:submit value="%{getText('generic.save')}" action="%{#mainAction}!save"/>
       <s:submit value="%{getText('generic.cancel')}" action="commonPingTargets" />
    </div>

</s:form>
