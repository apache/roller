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

<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/yui/assets/skins/sam/container.css'/>" />
<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/yui/menu/assets/menu.css'/>" />

<script type="text/javascript" src="<s:url value='/roller-ui/yui/yahoo-dom-event/yahoo-dom-event.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/container/container_core-min.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/container/container-min.js' />"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/menu/menu-min.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/dragdrop/dragdrop-min.js' />"></script>

<script type="text/javascript" src="<s:url value="/roller-ui/scripts/jquery-1.4.2.min.js" />"></script>


<style>
#tagAutoCompleteWrapper {
    width:25em; /* set width here or else widget will expand to fit its container */
    padding-bottom:2em;
}
</style>

<script type="text/javascript">
<!--
function fullPreviewMode() {
    window.open('<s:property value="previewURL" />', '_preview', '');
}
-->
</script>

<p class="subtitle">
    <s:text name="weblogEdit.subtitle.editEntry" >
        <s:param value="actionWeblog.handle" />
    </s:text>
</p>

<s:form id="entry" action="entryEdit!save" onsubmit="editorCleanup()">
    <s:hidden name="weblog" />
    <s:hidden name="bean.id" />
    <s:hidden name="bean.commentCount" />
    
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
                <s:if test="bean.published">
                    <span style="color:green; font-weight:bold">
                        <s:text name="weblogEdit.published" />
                        (<s:text name="weblogEdit.updateTime" />
                        <s:date name="entry.updateTime" format="dd/MM/yyyy hh:mm a" />)
                        
                        <s:if test='!getBooleanProp("weblogentry.editor.showFullPermalink")'>
                            &nbsp;&nbsp;&nbsp;&nbsp;<img src='<s:url value="/images/launch-link.png"/>' />
                            <a href='<s:property value="entry.permalink" />'><s:text name="weblogEdit.permaLink" /></a>
                        </s:if>
                        
                    </span>
                </s:if>
                <s:elseif test="bean.draft">
                    <span style="color:orange; font-weight:bold">
                        <s:text name="weblogEdit.draft" />
                        (<s:text name="weblogEdit.updateTime" />
                        <s:date name="entry.updateTime" />)
                    </span>
                </s:elseif>
                <s:elseif test="bean.pending">
                    <span style="color:orange; font-weight:bold">
                        <s:text name="weblogEdit.pending" />
                        (<s:text name="weblogEdit.updateTime" />
                        <s:date name="entry.updateTime" />)
                    </span>
                </s:elseif>
                <s:hidden name="bean.status" />
            </td>
        </tr>
    
        <s:if test='getBooleanProp("weblogentry.editor.showFullPermalink")'>
            <tr>
                <td class="entryEditFormLabel">
                    <label for="permalink"><s:text name="weblogEdit.permaLink" /></label>
                </td>
                <td>
                    <s:if test="bean.published">
                        <a href='<s:property value="entry.permalink" />'><s:property value="entry.permalink" /></a>
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
                <s:textfield id="tagAutoComplete" cssClass="entryEditTags" name="bean.tagsAsString" size="70" maxlength="255" tabindex="3" />
                <div id="tagAutoCompleteContainer"></div>
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
        <div >
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
            <!--
            if (document.layers) { // Netscape 4 hack
                var cal = new CalendarPopup();
            } else {
                var cal = new CalendarPopup("datetagdiv");
                document.write(cal.getStyles());
            }
            // -->
            </script>
            <s:textfield name="bean.dateString" size="12" />
            <a href="#" id="anchorCal" name="anchorCal"
               onclick="cal.select(document.getElementById('entry_bean_dateString'),'anchorCal','MM/dd/yy'); return false">
            <img src='<s:url value="/images/calendar.png"/>' class="calIcon" alt="Calendar" /></a>
            <s:property value="actionWeblog.timeZone" />
        </div>   
        <br />
        
        <s:checkbox name="bean.allowComments" />
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
        
        <s:text name="weblogEdit.enclosureURL" />: <s:textfield name="bean.enclosureURL" size="40" maxlength="255" />
        <s:if test="bean.enclosureURL != null">
            <s:text name="weblogEdit.enclosureType" />: <s:property value='entry.findEntryAttribute("att_mediacast_type")' />
            <s:text name="weblogEdit.enclosureLength" />: <s:property value='entry.findEntryAttribute("att_mediacast_length")' />
        </s:if>
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
        
        <s:url id="removeUrl" action="entryRemove">
            <s:param name="weblog" value="actionWeblog.handle" />
            <s:param name="removeId" value="%{entry.id}" />
        </s:url>
        <input type="button" value="<s:text name="weblogEdit.deleteEntry"/>" onclick="window.location='<s:property value="removeUrl" escape="false" />'" />
        
        <input type="button" name="fullPreview"
                            value='<s:text name="weblogEdit.fullPreviewMode" />'
                            onclick="fullPreviewMode()" />
    </div>
    
    
    <%-- ================================================================== --%>
    <%-- Trackback control --%>
    <s:if test="userAnAuthor">
        <br />
        <h2><s:text name="weblogEdit.trackback" /></h2>
        <s:text name="weblogEdit.trackbackUrl" />
        <br />
        <s:textfield name="trackbackUrl" size="80" maxlength="255" />

        <s:submit value="%{getText('weblogEdit.sendTrackback')}" action="entryEdit!trackback" />
    </s:if>
    
</s:form>


<script type="text/javascript">
YAHOO.example.RemoteCustomRequest = function() {
    // Use an XHRDataSource
    var oDS = new YAHOO.util.XHRDataSource("<s:property value="jsonAutocompleteUrl" />");
    // Set the responseType
    oDS.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
    // Define the schema of the JSON results
    oDS.responseSchema = {
        resultsList : "tagcounts",
        fields : ["tag"]
    };

    // Instantiate the AutoComplete
    var oAC = new YAHOO.widget.AutoComplete("tagAutoComplete", "tagAutoCompleteContainer", oDS);
    // Delimiter character, allow multiple tags to be chosen
    oAC.delimChar = [","," "];
    // Throttle requests sent
    oAC.queryDelay = .5;
    // The webservice needs additional parameters
    oAC.generateRequest = function(sQuery) {
        return "?format=json&prefix=" + sQuery ;
    };

    return {
        oDS: oDS,
        oAC: oAC
    };
}();
</script>
    