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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ taglib uri="http://sargue.net/jsptags/time" prefix="javatime" %>
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src="<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />"></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>

<script>
  $(function() {
    $("#confirm-delete").dialog({
      autoOpen: false,
      resizable: false,
      height:170,
      modal: true,
      buttons: {
        "<s:text name='generic.delete'/>": function() {
          document.location.href='<s:url action="entryEdit!remove" />?weblogId=<s:property value="weblogId"/>&bean.id=<s:property value="bean.id"/>';
          $( this ).dialog( "close" );
        },
        Cancel: function() {
          $( this ).dialog( "close" );
        }
      }
    });

    $("#delete-link").click(function(e) {
      e.preventDefault();
      $('#confirm-delete').dialog('open');
    });
  });
</script>


<style>
#tagAutoCompleteWrapper {
    width:40em; /* set width here or else widget will expand to fit its container */
    padding-bottom:2em;
}
</style>

<%-- Titling, processing actions different between entry add and edit --%>
<s:if test="actionName == 'entryEdit'">
    <s:set var="subtitleKey">weblogEdit.subtitle.editEntry</s:set>
    <s:set var="mainAction">entryEdit</s:set>
</s:if>
<s:else>
    <s:set var="subtitleKey">weblogEdit.subtitle.newEntry</s:set>
    <s:set var="mainAction">entryAdd</s:set>
</s:else>

<p class="subtitle">
    <s:text name="%{#subtitleKey}" >
        <s:param value="actionWeblog.handle" />
    </s:text>
</p>

<s:form id="entry">
    <sec:csrfInput/>
    <s:hidden name="weblogId" />
    <s:hidden name="bean.status" />
    <s:if test="actionName == 'entryEdit'">
        <s:hidden name="bean.id" />
    </s:if>

    <%-- ================================================================== --%>
    <%-- Title, category, dates and other metadata --%>

    <table class="entryEditTable" cellpadding="0" cellspacing="0" style="width:100%">

        <tr>
            <td class="entryEditFormLabel">
                <label for="title"><s:text name="weblogEdit.title" /></label>
            </td>
            <td>
                <s:textfield name="bean.title" size="70" maxlength="255" tabindex="1" onBlur="this.value=this.value.trim()" style="width:60%"/>
            </td>
        </tr>

                <javatime:format value="${firstEntry.pubTime}" pattern="${dateFormat}"/>


        <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="weblogEdit.status" /></label>
            </td>
            <td>
                <fmt:message key="generic.date.toStringFormat" var="dateFormat"/>
                <s:if test="bean.published">
                    <span style="color:green; font-weight:bold">
                        <s:text name="weblogEdit.published" />
                        (<s:text name="weblogEdit.updateTime" />
                        <javatime:format value="${entry.updateTime}" pattern="${dateFormat}"/>)
                    </span>
                </s:if>
                <s:elseif test="bean.draft">
                    <span style="color:orange; font-weight:bold">
                        <s:text name="weblogEdit.draft" />
                        (<s:text name="weblogEdit.updateTime" />
                        <javatime:format value="${entry.updateTime}" pattern="${dateFormat}"/>)
                    </span>
                </s:elseif>
                <s:elseif test="bean.pending">
                    <span style="color:orange; font-weight:bold">
                        <s:text name="weblogEdit.pending" />
                        (<s:text name="weblogEdit.updateTime" />
                        <javatime:format value="${entry.updateTime}" pattern="${dateFormat}"/>)
                    </span>
                </s:elseif>
                <s:elseif test="bean.scheduled">
                    <span style="color:orange; font-weight:bold">
                        <s:text name="weblogEdit.scheduled" />
                        (<s:text name="weblogEdit.updateTime" />
                        <javatime:format value="${entry.updateTime}" pattern="${dateFormat}"/>)
                    </span>
                </s:elseif>
                <s:else>
                    <span style="color:red; font-weight:bold"><s:text name="weblogEdit.unsaved" /></span>
                </s:else>
            </td>
        </tr>

        <s:if test="actionName == 'entryEdit'">
            <tr>
                <td class="entryEditFormLabel">
                    <label for="permalink"><s:text name="weblogEdit.permalink" /></label>
                </td>
                <td>
                    <s:if test="bean.published">
                        <a id="entry_bean_permalink" href='<s:property value="entry.permalink" />'><s:property value="entry.permalink" /></a>
                        <img src='<s:url value="/images/launch-link.png"/>' />
                    </s:if>
                    <s:else>
                        <s:property value="entry.permalink" />
                    </s:else>
                </td>
            </tr>
        </s:if>

        <tr>
            <td class="entryEditFormLabel">
                <label for="categoryId"><s:text name="weblogEdit.category" /></label>
            </td>
            <td>
                <s:select name="bean.categoryId" list="categories" listKey="id" listValue="name" size="1" />
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="title"><s:text name="weblogEdit.tags" /></label>
            </td>
            <td>
                <s:textfield id="tagAutoComplete" cssClass="entryEditTags" name="bean.tagsAsString" size="70"
                maxlength="255" tabindex="3" style="width:30%" onBlur="this.value=this.value.trim()"/>
            </td>
        </tr>
    </table>

    <%-- ================================================================== --%>
    <%-- Weblog editor --%>

    <s:include value="/WEB-INF/jsps/editor/EntryEditor.jsp" />

    <br />

    <%-- ================================================================== --%>
    <%-- plugin chooser --%>

    <s:if test="!weblogEntryPlugins.isEmpty">
        <div id="pluginControlToggle" class="controlToggle">
            <span id="ipluginControl">+</span>
            <a class="controlToggle" onclick="javascript:toggleControl('pluginControlToggle','pluginControl')">
            <s:text name="weblogEdit.pluginsToApply" /></a>
        </div>
        <div id="pluginControl" class="miscControl" style="display:none">
            <s:checkboxlist theme="strutsoverride" list="weblogEntryPlugins" listKey="name" listValue="name" name="bean.pluginsArray"/>
        </div>
    </s:if>


    <%-- ================================================================== --%>
    <%-- advanced settings  --%>

    <div id="miscControlToggle" class="controlToggle">
        <span id="imiscControl">+</span>
        <a class="controlToggle" onclick="javascript:toggleControl('miscControlToggle','miscControl')">
        <s:text name="weblogEdit.miscSettings" /></a>
    </div>
    <div id="miscControl" class="miscControl" style="display:none">

        <label for="link"><s:text name="weblogEdit.specifyPubTime" />:</label>
        <div>
            <s:textfield type="number" min="0" max="23" step="1" name="bean.hours"/>
            :
            <s:textfield type="number" min="0" max="59" step="1" name="bean.minutes"/>
            :
            <s:textfield type="number" min="0" max="59" step="1" name="bean.seconds"/>
            &nbsp;&nbsp;
            <script>
            $(function() {
                $( "#entry_bean_dateString" ).datepicker({
                    showOn: "button",
                    buttonImage: "../../images/calendar.png",
                    buttonImageOnly: true,
                    changeMonth: true,
                    changeYear: true
                });
            });
            </script>
            <s:textfield name="bean.dateString" size="12" readonly="true"/>
            <s:property value="actionWeblog.timeZone" />
        </div>
        <br />

        <s:if test="commentingEnabled">
            <s:text name="weblogEdit.allowComments" />
            <s:text name="weblogEdit.commentDays" />
            <s:select name="bean.commentDays" list="commentDaysList" size="1" listKey="left" listValue="right" />
            <br />
        </s:if>

        <br />

		<table>
			<tr>
				<td><s:text name="weblogEdit.searchDescription" />:<tags:help key="weblogEdit.searchDescription.tooltip"/></td>
				<td><s:textfield name="bean.searchDescription" size="80" maxlength="255" style="width:100%" onBlur="this.value=this.value.trim()"/> </td>
			</tr>
            <tr>
				<td><s:text name="weblogEdit.enclosureURL" />:<tags:help key="weblogEdit.enclosureURL.tooltip"/></td>
				<td><s:textfield name="bean.enclosureUrl" size="80" maxlength="255" style="width:100%" onBlur="this.value=this.value.trim()"/></td>
			</tr>
            <s:if test="actionName != 'entryAdd'">
                <tr>
                    <td></td>
                    <td>
                        <s:if test="bean.enclosureType != null && bean.enclosureType != ''">
                            <s:text name="weblogEdit.enclosureType" />: <s:property value='bean.enclosureType' />
                            <s:hidden name="bean.enclosureType"/>
                        </s:if>
                        <s:if test="bean.enclosureLength != null && bean.enclosureLength != 0">
                            <s:text name="weblogEdit.enclosureLength" />: <s:property value='bean.enclosureLength' />
                            <s:hidden name="bean.enclosureLength"/>
                        </s:if>
                    </td>
                </tr>
            </s:if>
		</table>
    </div>


    <%-- ================================================================== --%>
    <%-- the button box --%>

    <br>
    <div class="control">
        <span style="padding-left:7px">
            <s:submit value="%{getText('weblogEdit.save')}" action="%{#mainAction}!saveDraft" />
            <s:if test="actionName == 'entryEdit'">
                <input type="button" name="fullPreview"
                                    value="<s:text name='weblogEdit.fullPreviewMode' />"
                                    onclick="fullPreviewMode()" />
            </s:if>
            <s:if test="userAnAuthor">
                <s:submit value="%{getText('weblogEdit.post')}" action="%{#mainAction}!publish"/>
            </s:if>
            <s:else>
                <s:submit value="%{getText('weblogEdit.submitForReview')}" action="%{#mainAction}!publish"/>
            </s:else>
        </span>

        <s:if test="actionName == 'entryEdit'">
            <span style="float:right">
                <input type="button" value="<s:text name='weblogEdit.deleteEntry'/>" id="delete-link"/>
            </span>
        </s:if>
    </div>

</s:form>

<div id="confirm-delete" title="<s:text name='weblogEdit.deleteEntry'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name="weblogEntryRemove.areYouSure"/></p>
</div>

<script>
function fullPreviewMode() {
    window.open('<s:property value="previewURL" />');
}

//Get cookie to determine state of control
if (getCookie('control_miscControl') != null) {
    if(getCookie('control_miscControl') == 'true'){
        toggle('miscControl');
        togglePlusMinus('imiscControl');
    }
}
if (getCookie('control_pluginControl') != null) {
    if(getCookie('control_pluginControl') == 'true'){
        toggle('pluginControl');
        togglePlusMinus('ipluginControl');
    }
}
$(function() {
function split( val ) {
    return val.split( / \s*/ );
}
function extractLast( term ) {
    return split( term ).pop();
}
$( "#tagAutoComplete" )
    // don't navigate away from the field on tab when selecting an item
    .bind( "keydown", function( event ) {
        if ( event.keyCode === $.ui.keyCode.TAB && $( this ).autocomplete( "instance" ).menu.active ) {
            event.preventDefault();
        }
    })
    .autocomplete({
        delay: 500,
        source: function(request, response) {
            $.getJSON("${pageContext.request.contextPath}/tb-ui/authoring/rest/tagdata/<s:property value='%{actionWeblog.handle}' />",
            { prefix: extractLast( request.term ) },
            function(data) {
                response($.map(data.tagcounts, function (dataValue) {
                    return {
                        value: dataValue.name
                    };
                }))
            })
        },
        focus: function() {
            // prevent value inserted on focus
            return false;
        },
        select: function( event, ui ) {
            var terms = split( this.value );
            // remove the current input
            terms.pop();
            // add the selected item
            terms.push( ui.item.value );
            // add placeholder to get the space at the end
            terms.push( "" );
            this.value = terms.join( " " );
            return false;
        }
    });
});
</script>
