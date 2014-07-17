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

<link rel="stylesheet" type="text/css" media="all" href='<s:url value="/roller-ui/styles/jquery.ui.all.css"/>' />

<script type="text/javascript" src="<s:url value='/roller-ui/yui3/yui/yui-min.js' />"></script>
<script type="text/javascript" src="<s:url value="/roller-ui/scripts/jquery-2.1.1.min.js" />"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/scripts/jquery-ui.custom.min.js'/>"></script>

<style>
#tagAutoCompleteWrapper {
    width:40em; /* set width here or else widget will expand to fit its container */
    padding-bottom:2em;
}
</style>

<p class="subtitle">
    <s:text name="weblogEdit.subtitle.newEntry" >
        <s:param value="actionWeblog.handle" />
    </s:text>
</p>

<s:form id="entry" action="entryAdd!save">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />

    <%-- ================================================================== --%>
    <%-- Title, category, dates and other metadata --%>

    <table class="entryEditTable" cellpadding="0" cellspacing="0" width="100%">

        <tr>
            <td class="entryEditFormLabel">
                <label for="title"><s:text name="weblogEdit.title" /></label>
            </td>
            <td>
                <s:textfield name="bean.title" size="70" maxlength="255" tabindex="1" />
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="weblogEdit.status" /></label>
            </td>
            <td>
                <span style="color:red; font-weight:bold"><s:text name="weblogEdit.unsaved" /></span>
                <s:hidden name="bean.status" value="" />
            </td>
        </tr>

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
                <s:textfield id="tagAutoComplete" cssClass="entryEditTags" name="bean.tagsAsString" size="70" maxlength="255" tabindex="3" />
            </td>
        </tr>

        <s:if test="actionWeblog.enableMultiLang">
            <tr>
                <td class="entryEditFormLabel">
                    <label for="locale"><s:text name="weblogEdit.locale" /></label>
                </td>
                <td>
                    <s:select name="bean.locale" size="1" list="localesList" listValue="displayName" />
                </td>
            </tr>
        </s:if>
        <s:else>
            <s:hidden name="bean.locale"/>
        </s:else>

    </table>

    <%-- ================================================================== --%>
    <%-- Weblog edit or preview --%>

    <div style="width: 100%;"> <%-- need this div to control text-area size in IE 6 --%>
        <%-- include edit page --%>
        <div>
            <s:include value="%{editor.jspPage}" />
        </div>
    </div>

    <br />


    <%-- ================================================================== --%>
    <%-- plugin chooser --%>

    <s:if test="!entryPlugins.isEmpty">
        <div id="pluginControlToggle" class="controlToggle">
            <span id="ipluginControl">+</span>
            <a class="controlToggle" onclick="javascript:toggleControl('pluginControlToggle','pluginControl')">
            <s:text name="weblogEdit.pluginsToApply" /></a>
        </div>
        <div id="pluginControl" class="miscControl" style="display:none">
            <s:checkboxlist theme="roller" name="bean.plugins" list="entryPlugins" listKey="name" listValue="name" />
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

        <label for="link"><s:text name="weblogEdit.pubTime" /></label>
        <div>
            <s:select name="bean.hours" list="hoursList" />
            :
            <s:select name="bean.minutes" list="minutesList" />
            :
            <s:select name="bean.seconds" list="secondsList" />
            &nbsp;&nbsp;
            <script type="text/javascript" >
            $(function() {
                $( "#entry_bean_dateString" ).datepicker({
                    showOn: "button",
                    buttonImage: "/images/calendar.png",
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

        <s:checkbox name="bean.allowComments" onchange="onAllowCommentsChange()" />
        <s:text name="weblogEdit.allowComments" />
        <s:text name="weblogEdit.commentDays" />
        <s:select name="bean.commentDays" list="commentDaysList" size="1" listKey="key" listValue="value" />
        <br />

        <s:checkbox name="bean.rightToLeft" />
        <s:text name="weblogEdit.rightToLeft" />
        <br />

        <s:if test="authenticatedUser.hasGlobalPermission('admin')">
            <s:checkbox name="bean.pinnedToMain" />
            <s:text name="weblogEdit.pinnedToMain" />
            <br />
        </s:if>
        <br />

		<table>
			<tr>
				<td> <s:text name="weblogEdit.searchDescription" />: </td>
				<td> <s:textfield name="bean.searchDescription" size="60" maxlength="255" /> </td>
			</tr>
        	<tr>
				<td> <s:text name="weblogEdit.enclosureURL" />: </td>
				<td> <s:textfield name="bean.enclosureURL" size="40" maxlength="255" /> </td>
			</tr>
		</table>

    </div>


    <%-- ================================================================== --%>
    <%-- the button box --%>

    <br>
    <div class="control">
        <s:submit value="%{getText('weblogEdit.save')}" onclick="document.getElementById('entry_bean_status').value='DRAFT';" />
        <s:if test="userAnAuthor">
            <s:submit value="%{getText('weblogEdit.post')}" onclick="document.getElementById('entry_bean_status').value='PUBLISHED';"/>
        </s:if>
        <s:else>
            <s:submit value="%{getText('weblogEdit.submitForReview')}" onclick="document.getElementById('entry_bean_status').value='PENDING';"/>
        </s:else>
    </div>

</s:form>

<script type="text/javascript">
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
YUI().use(['autocomplete', 'autocomplete-filters', "datasource-io", "datasource-jsonschema"], function (Y) {

    var ds = new Y.DataSource.IO({
        source: "<s:property value="jsonAutocompleteUrl" />"
    });

    ds.plug({fn: Y.Plugin.DataSourceJSONSchema, cfg: {
        schema: {
            resultListLocator: 'tagcounts',
            resultFields: ['tag']
        }
    }});

    Y.one('#tagAutoComplete').plug(Y.Plugin.AutoComplete, {
      queryDelimiter: ' ',
      requestTemplate: '?format=json&prefix={query}',
      resultTextLocator: 'tag',
      source: ds
    });
});
</script>
