<!--
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
-->
<%@ include file="/taglibs.jsp" %>
<%@ page import="org.apache.roller.pojos.*" %>
<%@ page import="org.apache.roller.ui.authoring.struts.formbeans.WeblogEntryFormEx" %>
<%@ page import="org.apache.roller.ui.authoring.struts.actions.WeblogEntryPageModel" %>
<%
WeblogEntryPageModel model = (WeblogEntryPageModel)request.getAttribute("model");
try {
%>
<script type="text/javascript">
<!--
function postWeblogEntry() {
    document.weblogEntryFormEx.submit();
}
function previewMode() {
    document.weblogEntryFormEx.method.value = "preview";
    postWeblogEntry();
}
function returnToEditMode() {
    document.weblogEntryFormEx.method.value = "returnToEditMode";
    postWeblogEntry();
}
function deleteWeblogEntry() {
    document.weblogEntryFormEx.method.value = "removeOk";
    postWeblogEntry();
}
function sendTrackback() {
    document.weblogEntryFormEx.method.value = "sendTrackback";
    postWeblogEntry();
}
function saveDraft() {
    document.weblogEntryFormEx.status.value = "DRAFT"; 
    document.weblogEntryFormEx.method.value = "save";
    postWeblogEntry();
}
function publish() {
    <c:choose>
	    <c:when test="${model.userAuthorizedToAuthor}">
	        document.weblogEntryFormEx.status.value = "PUBLISHED";
	    </c:when>
	    <c:otherwise>
            document.weblogEntryFormEx.status.value = "PENDING";
	    </c:otherwise>
    </c:choose>
    document.weblogEntryFormEx.method.value = "save";
    postWeblogEntry();
}
-->
</script>

<c:choose>
    <c:when test="${empty weblogEntryFormEx.id}">
        <p class="subtitle">
            <fmt:message key="weblogEdit.subtitle.newEntry" >
                <fmt:param value="${model.weblogEntry.website.handle}" />
            </fmt:message>
        </p>
    </c:when>
    <c:otherwise>
        <p class="subtitle">
            <fmt:message key="weblogEdit.subtitle.editEntry" >
                <fmt:param value="${model.weblogEntry.website.handle}" />
            </fmt:message>
        </p>
    </c:otherwise>
</c:choose>

<html:form action="/roller-ui/authoring/weblog" method="post" focus="title">
    <html:hidden property="day"/>
    <html:hidden property="id"/>
    <html:hidden property="creatorId"/>
    <html:hidden property="websiteId"/>
    <html:hidden property="anchor"/>
    <html:hidden property="status"/>
    <html:hidden property="link"/>
    <html:hidden property="contentType" />
    <html:hidden property="contentSrc" />
    <html:hidden name="method" property="method" value="save"/>
    
    <%-- ================================================================== --%>
    <%-- Title, category, dates and other metadata --%>

    <table class="entryEditTable" cellpadding="0" cellspacing="0" width="100%">   

       <tr><td class="entryEditFormLabel">
          <label for="title">
             <fmt:message key="weblogEdit.title" />
          </label>
       </td><td>
           <html:text property="title" style="width:100%" maxlength="255" tabindex="1" />
       </td></tr>
       
       <tr><td class="entryEditFormLabel">
          <label for="status">
             <fmt:message key="weblogEdit.status" />
          </label>
       </td><td>
          <c:if test="${!empty weblogEntryFormEx.id}">
             <c:if test="${weblogEntryFormEx.published}">
                <span style="color:green; font-weight:bold">
                   <fmt:message key="weblogEdit.published" />
                   (<fmt:message key="weblogEdit.updateTime" />
                   <fmt:formatDate value="${weblogEntryFormEx.updateTime}" type="both"
                      dateStyle="short" timeStyle="short" />)
                </span>
            </c:if>
            <c:if test="${weblogEntryFormEx.draft}">
                <span style="color:orange; font-weight:bold">
                   <fmt:message key="weblogEdit.draft" />
                   (<fmt:message key="weblogEdit.updateTime" />
                   <fmt:formatDate value="${weblogEntryFormEx.updateTime}" type="both"
                      dateStyle="short" timeStyle="short" />)
                </span>
            </c:if>
            <c:if test="${weblogEntryFormEx.pending}">
                <span style="color:orange; font-weight:bold">
                   <fmt:message key="weblogEdit.pending" />
                   (<fmt:message key="weblogEdit.updateTime" />
                   <fmt:formatDate value="${weblogEntryFormEx.updateTime}" type="both"
                      dateStyle="short" timeStyle="short" />)
                </span>
            </c:if>
        </c:if>
        <c:if test="${empty weblogEntryFormEx.id}">
           <span style="color:red; font-weight:bold"><fmt:message key="weblogEdit.unsaved" /></span>
        </c:if>
    </td></tr>
        
    <tr><td class="entryEditFormLabel">
       <label for="categoryId"><fmt:message key="weblogEdit.category" /></label>
    </td><td>
       <html:select property="categoryId" size="1" tabindex="4">
       <html:optionsCollection name="model" property="categories" value="id" label="path"  />
       </html:select>
    </td></tr>
    
    <c:choose>
        <c:when test="${model.weblog.enableMultiLang}">
            <tr><td class="entryEditFormLabel">
                <label for="locale"><fmt:message key="weblogEdit.locale" /></label>
            </td><td>
                <html:select property="locale" size="1" tabindex="5">
                    <html:options collection="locales" property="value" labelProperty="label"/>
                </html:select>
            </td></tr>
        </c:when>
        <c:otherwise>
            <html:hidden property="locale"/>
        </c:otherwise>
    </c:choose>
    
    <tr>
        <td class="entryEditFormLabel">
            <label for="link"><fmt:message key="weblogEdit.pubTime" /></label>
        </td>
        <td>
        <div>
           <html:select property="hours">
               <html:options name="model" property="hoursList" />
            </html:select>
           :
           <html:select property="minutes" >
               <html:options name="model" property="minutesList" />
           </html:select>
           :
           <html:select property="seconds">
               <html:options name="model" property="secondsList" />
           </html:select>
           &nbsp;&nbsp;
           <roller:Date property="dateString" dateFormat='<%= model.getShortDateFormat() %>' />
           <c:out value="${model.weblogEntry.website.timeZone}" />
        </div>
    </td></tr>
    
    <c:if test="${!empty weblogEntryFormEx.id}">
        <tr><td class="entryEditFormLabel">
            <label for="categoryId">
               <fmt:message key="weblogEdit.permaLink" />
            </label>
            </td><td>
            <a href='<c:out value="${model.permaLink}" />'>
               <c:out value="${model.permaLink}" />
            </a>
        </td></tr>
    </c:if>
    
   </table>
    
   
    <%-- ================================================================== --%>
    <%-- Weblog edit or preview --%>
    
    <%-- EDIT MODE --%>
    <c:if test="${model.editMode}">
    <div style="width: 100%;"> <%-- need this div to control text-area size in IE 6 --%>
       <%-- include edit page --%>
       <div >
            <jsp:include page='<%= "/roller-ui/authoring/editors/"+model.getEditorPage() %>' />
       </div>
     </div>
    </c:if>
    
    <%-- PREVIEW MODE --%>
    <c:if test="${model.previewMode}" >
        <html:hidden property="text" />
        <html:hidden property="summary" />

        <br />
        <br />
        <div class="centerTitle"><fmt:message key="weblogEdit.previewSummary" /></div>
        <div class="previewEntrySummary">
           <roller:ShowEntrySummary name="model" property="weblogEntry" />
        </div>
        
        <br />
        <br />
        <div class="centerTitle"><fmt:message key="weblogEdit.previewContent" /></div>
        <div class="previewEntryContent">
           <roller:ShowEntryContent name="model" property="weblogEntry" />
        </div>
    </c:if>

        
   <%-- ================================================================== --%>
   <%-- the button box --%>

   <br></br>
   <div class="control">

        <%-- save draft and post buttons: only in edit and preview mode --%>
        <c:if test="${model.editMode || model.previewMode}" >
        
            <c:choose>
            
	            <c:when test="${model.userAuthorizedToAuthor}" >        
                    <input type="button" name="post"
	                       value='<fmt:message key="weblogEdit.post" />'
	                       onclick="publish()" />
	                <input type="button" name="draft"
	                       value='<fmt:message key="weblogEdit.save" />'
	                       onclick="saveDraft()" />                                      
	                <c:if test="${!empty weblogEntryFormEx.id}">
	                    <input type="button" name="draft"
	                           value='<fmt:message key="weblogEdit.deleteEntry" />'
	                           onclick="deleteWeblogEntry()" />
	                </c:if>
	            </c:when> 
	            
	            <c:when test="${model.userAuthorized}" > 
                    <c:if test="${weblogEntryFormEx.status == 'DRAFT'}">       
		                <input type="button" name="post"
		                       value='<fmt:message key="weblogEdit.submitForReview" />'
		                       onclick="publish()" />
		                <input type="button" name="draft"
		                       value='<fmt:message key="weblogEdit.save" />'
		                       onclick="saveDraft()" />                  
		                <%-- only show delete button for saved entries --%>
		                <c:if test="${!empty weblogEntryFormEx.id}">
		                    <input type="button" name="draft"
		                           value='<fmt:message key="weblogEdit.deleteEntry" />'
		                           onclick="deleteWeblogEntry()" />
		                </c:if>   
                    </c:if>            
	            </c:when>
                
            </c:choose>
             
        </c:if>

        <%-- edit mode buttons --%>
        <c:if test="${model.editMode}" >

            <input type="button" name="preview"
                   value='<fmt:message key="weblogEdit.previewMode" />'
                   onclick="previewMode()" />

        </c:if>

        <%-- preview mode buttons --%>
        <c:if test="${model.previewMode}" >
            <input type="button" name="edit" value='<fmt:message key="weblogEdit.returnToEditMode" />'
                   onclick="returnToEditMode()" />
        </c:if>

    </div>

    
    <%-- ================================================================== --%>
    <%-- Other settings --%>

    <h2><fmt:message key="weblogEdit.otherSettings" /></h2>
     
  <%-- ================================================================== --%>
  <%-- comment settings --%>
   
  <div id="commentControlToggle" class="controlToggle">
  <span id="icommentControl">+</span>
  <a class="controlToggle" onclick="javascript:toggleControl('commentControlToggle','commentControl')">
     <fmt:message key="weblogEdit.commentSettings" />
  </a>
  </div>
  <div id="commentControl" class="miscControl" style="display:none">
     <html:checkbox property="allowComments" onchange="onAllowCommentsChange()" />
     <fmt:message key="weblogEdit.allowComments" />
     <fmt:message key="weblogEdit.commentDays" />
     <html:select property="commentDays">
         <html:option key="weblogEdit.unlimitedCommentDays" value="0"  />
         <html:option key="weblogEdit.days1" value="1"  />
         <html:option key="weblogEdit.days2" value="2"  />
         <html:option key="weblogEdit.days3" value="3"  />
         <html:option key="weblogEdit.days4" value="4"  />
         <html:option key="weblogEdit.days5" value="5"  />
         <html:option key="weblogEdit.days7" value="7"  />
         <html:option key="weblogEdit.days10" value="10"  />
         <html:option key="weblogEdit.days20" value="20"  />
         <html:option key="weblogEdit.days30" value="30"  />
         <html:option key="weblogEdit.days60" value="60"  />
         <html:option key="weblogEdit.days90" value="90"  />
     </html:select>
     <br />
  </div>

  <%-- ================================================================== --%>
  <%-- plugin chooser --%>

  <c:if test="${model.hasPagePlugins}">
      <div id="pluginControlToggle" class="controlToggle">
      <span id="ipluginControl">+</span>
      <a class="controlToggle" onclick="javascript:toggleControl('pluginControlToggle','pluginControl')">
         <fmt:message key="weblogEdit.pluginsToApply" /></a>
      </div>
      <div id="pluginControl" class="miscControl" style="display:none">
        <logic:iterate id="plugin" type="org.apache.roller.model.WeblogEntryPlugin"
            collection="<%= model.getPagePlugins() %>">
            <html:multibox property="pluginsArray"
                 title="<%= plugin.getName() %>" value="<%= plugin.getName() %>"
                 styleId="<%= plugin.getName() %>"/></input>
            <label for="<%= plugin.getName() %>"><%= plugin.getName() %></label>
            <a href="javascript:void(0);" onmouseout="return nd();"
            onmouseover="return overlib('<%= plugin.getDescription() %>', STICKY, MOUSEOFF, TIMEOUT, 3000);">?</a>
            <br />
        </logic:iterate>
      </div>
  </c:if>

  <%-- ================================================================== --%>
  <%-- misc settings  --%>

  <div id="miscControlToggle" class="controlToggle">
  <span id="imiscControl">+</span>
  <a class="controlToggle" onclick="javascript:toggleControl('miscControlToggle','miscControl')">
     <fmt:message key="weblogEdit.miscSettings" /></a>
  </div>
  <div id="miscControl" class="miscControl" style="display:none">

     <html:checkbox property="rightToLeft" />
     <fmt:message key="weblogEdit.rightToLeft" />
     <br />

     <c:if test="${model.rollerSession.globalAdminUser}">
         <html:checkbox property="pinnedToMain" />
         <fmt:message key="weblogEdit.pinnedToMain" />
         <br />
     </c:if>
     <c:if test="${!model.rollerSession.globalAdminUser}">
         <html:hidden property="pinnedToMain" />
     </c:if>

  </div>

  <%-- ================================================================== --%>
  <%-- MediaCast settings  --%>

  <div id="mediaCastControlToggle" class="controlToggle">
  <span id="imediaCastControl">+</span>
  <a class="controlToggle" onclick="javascript:toggleControl('mediaCastControlToggle','mediaCastControl')">
     MediaCast Settings</a>
  </div>
  <div id="mediaCastControl" class="miscControl" style="display:none">
  <%
  WeblogEntryFormEx form = model.getWeblogEntryForm();
  String att_url = (String)form.getAttributes().get("att_mediacast_url");
  att_url = (att_url == null) ? "" : att_url;
  %>
     <b>URL:</b> <input name="att_mediacast_url" type="text" size="80" maxlength="255" value='<%= att_url %>' />
<%
  String att_type = (String)form.getAttributes().get("att_mediacast_type");
  String att_length = (String)form.getAttributes().get("att_mediacast_length");
%>
<% if (att_url != null && att_type != null && att_length != null) { %>
     <b>Type:</b> <%= att_type %>
     <b>Length:</b> <%= att_length %>
<% } else if (att_url != null && att_url.trim().length()!=0) { %>
     <span style="color:red">MediaCast URL is invalid</span>
<% } %>
  </div>


    <%-- ================================================================== --%>
    <%-- Trackback control --%>
    <c:if test="${!empty weblogEntryFormEx.id && model.userAuthorizedToAuthor}">
        <br />
        <a name="trackbacks"></a>
        <h2><fmt:message key="weblogEdit.trackback" /></h2>
        <fmt:message key="weblogEdit.trackbackUrl" /><br />
        <html:text property="trackbackUrl" size="80" maxlength="255" />

        <input type="button" name="draft"
            value='<fmt:message key="weblogEdit.sendTrackback" />'
            onclick="sendTrackback()" />

    </c:if>

</html:form>

<%--
Add this back in once it has been properly internationalized
<iframe id="keepalive" width="100%" height="25" style="border: none;"
        src="<%= request.getContextPath() %>/keepalive.jsp" ></iframe>
--%>

<script type="text/javascript">
<!--
try {Start();} catch (e) {};
-->
</script>

<% } catch (Throwable e) { e.printStackTrace(); } %>


