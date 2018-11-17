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

<p class="subtitle"> <s:text name="%{#subtitleKey}"/> </p>

<s:form theme="bootstrap" cssClass="form-horizontal">
    <s:hidden name="salt"/>

    <s:if test="actionName == 'commonPingTargetEdit'"> <s:hidden name="bean.id"/> </s:if>

    <s:textfield name="bean.name" size="30" maxlength="30" style="width:50%"
        onchange="validate()" onkeyup="validate()"
        label="%{getText('generic.name')}" />

    <s:textfield name="bean.pingUrl" size="100" maxlength="255" style="width:50%"
        onchange="validate()" onkeyup="validate()"
        label="%{getText('pingTarget.pingUrl')}" />

    <s:submit id="save-button" cssClass="btn btn-default"
        value="%{getText('generic.save')}" action="%{#mainAction}!save"/>

    <s:submit cssClass="btn" value="%{getText('generic.cancel')}" action="commonPingTargets"/>

</s:form>

<script type="application/javascript">

    function validate() {
        var savePingTargetButton = $('#save-button:first');
        var name = $('#commonPingTargetAdd_bean_name:first').val().trim();
        var url = $('#commonPingTargetAdd_bean_pingUrl:first').val().trim();
        if ( name.length > 0 && url.length > 0 && isValidUrl(url) ) {
            savePingTargetButton.attr("disabled", false);
        } else {
            savePingTargetButton.attr("disabled", true);
        }
    }

    function isValidUrl(url) {
        if (/^(http|https|ftp):\/\/[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/i.test(url)) {
            return true;
        } else {
            return false;
        }
    }

    $( document ).ready(function() {
        var savePingTargetButton = $('#save-button:first');
        savePingTargetButton.attr("disabled", true);
    });

</script>
