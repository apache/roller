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
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.0/jquery-ui.min.css"/>' />
<script src='<s:url value="/tb-ui/scripts/jquery-2.1.1.min.js" />'></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.0/jquery-ui.min.js"/>'></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var msg= {
    confirmLabel: '<s:text name="generic.confirm"/>',
    saveLabel: '<s:text name="generic.save"/>',
    cancelLabel: '<s:text name="generic.cancel"/>',
    editTitle: '<s:text name="generic.edit"/>',
    addTitle: '<s:text name="pingTarget.addTarget"/>',
    pingTargetEnable: '<s:text name="pingTarget.enable"/>',
    pingTargetEnabledIndicator: '<s:text name="pingTarget.enabled"/>',
    pingTargetDisable: '<s:text name="pingTarget.disable"/>',
    pingTargetDisabledIndicator: '<s:text name="pingTarget.disabled"/>'
};
</script>
<script src="<s:url value='/tb-ui/scripts/pingtargets.js'/>"></script>

<p class="subtitle">
    <s:text name="commonPingTargets.subtitle" />
</p>

<p/><s:text name="commonPingTargets.explanation"/><p/>

<table class="rollertable">

<%-- Headings --%>
<tr>
    <th width="20%%"><s:text name="generic.name" /></th>
    <th width="55%"><s:text name="pingTarget.pingUrl" /></th>
    <th width="15%" colspan="2"><s:text name="pingTarget.autoEnabled" /></th>
    <th width="5%"><s:text name="generic.edit" /></th>
    <th width="5%"><s:text name="pingTarget.remove" /></th>
</tr>

<s:form id="pingTargetsForm" action="commonPingTargets">
  <s:hidden name="salt" />
</s:form>

<%-- Listing of current common targets --%>
<s:iterator id="pingTarget" value="pingTargets" status="rowstatus">

    <s:if test="#rowstatus.odd == true">
        <tr class="rollertable_odd">
    </s:if>
    <s:else>
        <tr class="rollertable_even">
    </s:else>

    <td id='ptname-<s:property value="#pingTarget.id"/>'><s:property value="#pingTarget.name" /></td>

    <td id='pturl-<s:property value="#pingTarget.id"/>'><s:property value="#pingTarget.pingUrl" /></td>

    <td align="center">
       <span style="font-weight: bold;" id="enablestate-<s:property value='#pingTarget.id'/>">
       <s:if test="#pingTarget.autoEnabled">
           <s:text name="pingTarget.enabled"/>
       </s:if>
       <s:else>
           <s:text name="pingTarget.disabled"/>
       </s:else>
       </span>
    </td>

    <td align="center" >
       <a href="#" class="enable-toggle" data-id='<s:property value="#pingTarget.id"/>' data-enabled='<s:property value="#pingTarget.autoEnabled"/>'>
       <s:if test="#pingTarget.autoEnabled">
           <s:text name="pingTarget.disable"/>
       </s:if>
       <s:else>
           <s:text name="pingTarget.enable"/>
       </s:else>
       </a>
    </td>

    <td align="center">
        <a class="edit-link" data-id='<s:property value="#pingTarget.id"/>'>
            <img src='<s:url value="/images/page_white_edit.png"/>' border="0" alt="<s:text name="generic.edit" />" />
        </a>
    </td>

    <td align="center">
        <a class="delete-link" data-id='<s:property value="#pingTarget.id"/>'>
            <img src='<s:url value="/images/delete.png"/>' border="0" alt="<s:text name="pingTarget.remove" />" />
        </a>
    </td>

    </tr>
</s:iterator>

</table>

<div style="padding: 4px; font-weight: bold;">
    <img src='<s:url value="/images/add.png"/>' border="0" alt="icon" /><a href="#" id="add-link"><s:text name="pingTarget.addTarget" /></a>
</div>

<div id="confirm-delete" title="<s:text name='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name="pingTarget.confirmCommonRemove"/></p>
</div>

<div id="pingtarget-edit" style="display:none">
    <span id="pingtarget-edit-error" style="display:none"><s:text name='pingTarget.nameOrUrlNotUnique'/></span>
    <form>
    <table>
        <tr>
            <td style="width:30%"><label for="pingtarget-edit-name"><s:text name='generic.name'/></label></td>
            <td><input id="pingtarget-edit-name" maxlength="40" size="50" onBlur="this.value=this.value.trim()"/></td>
        </tr>
        <tr>
            <td><label for="pingtarget-edit-url"><s:text name='pingTarget.pingUrl'/></label></td>
            <td><input id="pingtarget-edit-url" maxlength="128" size="50" onBlur="this.value=this.value.trim()"/></td>
        </tr>
    </table>
    </form>
</div>
