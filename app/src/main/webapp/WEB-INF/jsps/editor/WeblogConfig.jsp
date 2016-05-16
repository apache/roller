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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src='<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>

<p class="subtitle">
   <s:text name="websiteSettings.subtitle" >
       <s:param value="actionWeblog.handle" />
   </s:text>
</p>  

<script>
  $(function() {
    $("#confirm-delete").dialog({
      autoOpen: false,
      resizable: true,
      height:310,
      modal: true,
      buttons: {
        "<s:text name='generic.delete'/>": function() {
          document.weblogConfigForm.action='<s:url action="weblogConfig!remove" />';
          document.weblogConfigForm.submit();
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


<s:form id="weblogConfigForm">
    <sec:csrfInput/>
    <s:hidden name="weblog" value="%{actionWeblog.handle}" />

<table class="formtableNoDesc">

    <%-- ***** General settings ***** --%>
    
    <tr>
        <td colspan="3"><h2><s:text name="websiteSettings.generalSettings" /></h2></td>
    </tr>
    
    <tr>
        <td class="label"><s:text name="websiteSettings.websiteTitle" />
        <td class="field"><s:textfield name="bean.name" size="40" maxlength="255" onBlur="this.value=this.value.trim()"/></td>
    </tr>

    <tr>
        <td class="label"><s:text name="generic.tagline" /></td>
        <td class="field"><s:textfield name="bean.tagline" size="40" maxlength="255" onBlur="this.value=this.value.trim()"/></td>
    </tr>
    
    <tr>
        <td class="label"><s:text name="websiteSettings.about" /></td>
        <td class="field"><s:textarea name="bean.about" rows="3" cols="40" maxlength="255" onBlur="this.value=this.value.trim()"/></td>
    </tr>
    
    <tr>
        <td class="label"><s:text name="websiteSettings.editor" /></td>
        <td class="field">
            <s:select name="bean.editorPage" size="1" list="editorsList" listKey="left" listValue="getText(right)" />
       </td>
    </tr>
    
    <tr>
        <td class="label"><s:text name="websiteSettings.active" /></td>
        <td class="field"><s:checkbox name="bean.visible" /></td>
    </tr>
    
    <tr>
        <td class="label"><s:text name="websiteSettings.entryDisplayCount" /></td>
        <td class="field"><s:textfield name="bean.entryDisplayCount" size="3" onBlur="this.value=this.value.trim()"/></td>
    </tr>

    <tr>
        <td class="label"><s:text name="createWebsite.locale" />
        <td class="field">
            <s:select name="bean.locale" size="1" list="localesList" listValue="displayName" />
        </td>
    </tr>
    
    <tr>
        <td class="label"><s:text name="createWebsite.timeZone" />
        <td class="field">
            <s:select name="bean.timeZone" size="1" list="timeZonesList" />
        </td>
    </tr>
    
    
    <%-- ***** Comment settings ***** --%>
    
    <s:if test="getBooleanProp('users.comments.enabled')">
        <tr>
            <td colspan="3"><h2><s:text name="websiteSettings.commentSettings" /></h2></td>
        </tr>

        <tr>
            <td class="label"><s:text name="websiteSettings.allowComments" /></td>
            <td class="field"><s:checkbox name="bean.allowComments" /></td>
        </tr>

        <s:if test="!getBooleanProp('users.moderation.required')">
            <tr>
                <td class="label"><s:text name="websiteSettings.approveComments" /></td>
                <td class="field"><s:checkbox name="bean.approveComments" /></td>
            </tr>
        </s:if>

        <s:if test="getBooleanProp('users.comments.emailnotify')">
            <tr>
                <td class="label"><s:text name="websiteSettings.emailComments" /></td>
                <td class="field"><s:checkbox name="bean.emailComments"/></td>
            </tr>
        </s:if>

        <tr>
            <td class="label"><s:text name="websiteSettings.defaultCommentDays" /></td>
            <td class="field">
                <s:select name="bean.defaultCommentDaysString" list="commentDaysList" size="1" listKey="left" listValue="right" />
        </tr>

        <tr>
            <td class="label"><s:text name="websiteSettings.applyCommentDefaults" /></td>
            <td class="field"><s:checkbox name="bean.applyCommentDefaults" /></td>
        </tr>
    </s:if>

    <%-- ***** Plugins "formatting" settings ***** --%>
    <s:if test="!weblogEntryPlugins.isEmpty">
        <tr>
            <td colspan="3"><h2><s:text name="websiteSettings.formatting" /></h2></td>
        </tr>

        <tr>
            <td class="label"><s:text name="websiteSettings.label1" /> <br /><s:text name="websiteSettings.label2" /></td>
            <td class="field">
                <s:checkboxlist theme="strutsoverride" list="weblogEntryPlugins" listKey="name" listValue="name" name="bean.defaultPluginsArray"/>
            </td>
        </tr>
    </s:if>
    <s:else>
        <s:hidden name="defaultPlugins" />
    </s:else>


    <%-- ***** Spam prevention settings ***** --%>
    
    <tr>
        <td colspan="3"><h2><s:text name="websiteSettings.spamPrevention" /></h2></td>
    </tr>

    <tr>
        <td class="label"><s:text name="websiteSettings.ignoreUrls" /></td>
        <td class="field"><s:textarea name="bean.blacklist" rows="7" cols="40" onBlur="this.value=this.value.trim()"/></td>
    </tr>


    <%-- ***** Web analytics settings ***** --%>

    <s:if test="getBooleanProp('analytics.code.override.allowed')">
        <tr>
            <td colspan="3"><h2><s:text name="configForm.webAnalytics" /></h2></td>
        </tr>

        <tr>
            <td class="label"><s:text name="websiteSettings.analyticsTrackingCode" /></td>
            <td class="field"><s:textarea name="bean.analyticsCode" rows="10" cols="70" maxlength="1200" onBlur="this.value=this.value.trim()"/></td>
        </tr>
    </s:if>

</table>

<br />
<div class="control">
    <s:submit value="%{getText('websiteSettings.button.update')}" action="weblogConfig!save"/>
</div>
        
<br />
<br />

<h2><s:text name="websiteSettings.removeWebsiteHeading" /></h2>

<p>
    <s:text name="websiteSettings.removeWebsite" /><br/><br/>
    <span class="warning">
        <s:text name="websiteSettings.removeWebsiteWarning" />
    </span>
</p>

<br />

<s:submit value="%{getText('websiteSettings.button.remove')}" class="delete-link"/>
    <br />
    <br />
    <br />
</s:form>

<div id="confirm-delete" title="<s:text name='websiteRemove.title'/>" style="display:none">
    <s:text name="websiteRemove.youSure">
        <s:param value="actionWeblog.name" />
    </s:text>
    <br/>
    <br/>
    <span class="warning">
        <s:text name="websiteSettings.removeWebsiteWarning" />
    </span>
</div>
