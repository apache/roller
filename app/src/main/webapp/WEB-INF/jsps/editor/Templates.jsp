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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ taglib uri="http://sargue.net/jsptags/time" prefix="javatime" %>
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src='<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>

<script>
  $(function() {
    $("#confirm-delete").dialog({
      autoOpen: false,
      resizable: true,
      height:310,
      modal: true,
      buttons: {
        "<s:text name='generic.delete'/>": function() {
          document.templatesForm.action='<s:url action="templates!remove" />';
          document.templatesForm.submit();
          $( this ).dialog( "close" );
        },
        Cancel: function() {
          $( this ).dialog( "close" );
        }
      }
    });

    $(".delete-link").click(function(e) {
      e.preventDefault();
      $('#confirm-delete').dialog('open');
    });
  });
</script>

<p class="subtitle">
   <s:text name="templates.subtitle" >
       <s:param value="actionWeblog.handle" />
   </s:text>
</p>
<p class="pagetip">
   <s:text name="templates.tip" />
</p>

<s:form id="templatesForm">
    <sec:csrfInput/>
    <s:hidden name="weblogId" value="%{actionWeblog.id}" />

<%-- table of pages --%>
<table class="rollertable">

    <tr>
        <th width="17%"><s:text name="generic.name"/></th>
        <th width="20%"><s:text name="templates.path"/></th>
        <th width="34%"><s:text name="templates.role"/></th>
        <th width="8%"><s:text name="templates.source"/></th>
        <th width="13%"><s:text name="generic.lastModified"/></th>
        <th width="4%"><s:text name="generic.view"/></th>
        <th width="4%"><input type="checkbox" onclick="toggleFunction(this.checked,'idSelections');"/></th>
    </tr>
    <fmt:message key="generic.date.toStringFormat" var="dateFormat"/>
    <s:iterator var="p" value="templates" status="rowstatus">
        <s:if test="#rowstatus.odd == true">
            <tr class="rollertable_odd">
        </s:if>
        <s:else>
            <tr class="rollertable_even">
        </s:else>

            <td style="vertical-align:middle">
                <s:if test="#p.derivation.name() != 'SHARED'">
                    <s:url var="edit" action="templateEdit">
                        <s:param name="weblogId" value="%{actionWeblog.id}" />
                        <s:param name="bean.id" value="#p.id" />
                    </s:url>
                </s:if>
                <s:else>
                    <s:url var="edit" action="templateEdit">
                        <s:param name="weblogId" value="%{actionWeblog.id}" />
                        <s:param name="bean.name" value="#p.name" />
                    </s:url>
                </s:else>
                <s:a href="%{edit}"><s:property value="#p.name" /></s:a>
            </td>

            <td style="vertical-align:middle">
                <s:if test="#p.role.accessibleViaUrl">
                    <s:property value="#p.relativePath" />
                </s:if>
            </td>

            <td style="vertical-align:middle">
                <s:if test="#p.role.singleton || #p.description == null || #p.description == ''">
                    <s:property value="#p.role.readableName"/>
                </s:if>
                <s:else>
                    <s:property value="#p.role.readableName"/>: <s:property value="#p.description" />
                </s:else>
            </td>

            <td style="vertical-align:middle"><s:property value="#p.derivation.readableName" /></td>

            <td>
                <s:if test="#p.lastModified != null">
                    <s:set var="tempTime" value="#p.lastModified"/>
                    <javatime:format value="${tempTime}" pattern="${dateFormat}"/>
                </s:if>
            </td>

            <td align="center" style="vertical-align:middle">
                <s:if test="#p.role.accessibleViaUrl">
                    <img src='<s:url value="/images/world_go.png"/>' border="0" alt="icon"
                    onclick="launchPage('<s:property value="actionWeblog.absoluteURL"/>page/<s:property value="#p.relativePath" />')"/>
                </s:if>
            </td>

            <td class="center" style="vertical-align:middle">
                <s:if test="#p.derivation.name() != 'SHARED'">
                    <input type="checkbox" name="idSelections" value="<s:property value="#p.id" />" />
                </s:if>
            </td>
        </tr>
    </s:iterator>
</table>

<br/>

<s:if test="!templates.isEmpty">
	<div class="control">
		<s:submit class="delete-link" value="%{getText('templates.deleteselected')}" />

    <span style="float:right">
        <s:submit id="switch-theme-button" action="themeEdit" value="%{getText('templates.switchTheme')}" />
    </span>
	</div>
</s:if>

<script>
function launchPage(url) {
    window.open(url, '_blank');
}
</script>
</s:form>

<div id="confirm-delete" title="<s:text name='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name="templateRemoves.youSure" />
	<br/>
	<br/>
	<span class="warning">
		<s:text name="templateRemoves.youSureWarning" />
	</span>
  </p>
</div>
