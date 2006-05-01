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
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

<%-- Inserts page content for preview. --%>

   <div class="previewnote">
      <b>This is a PREVIEW!  Hit "back" on your browser to go back to the editor,
      or hit "Save" if you're happy with what you see.</b>
   </div>

   <p><hr /></p>

   <div class="previewcontent">
      <wiki:Translate><%=pageContext.getAttribute("usertext",PageContext.REQUEST_SCOPE)%></wiki:Translate>
   </div>

   <br clear="all" />

   <p><hr /></p>

   <div class="previewnote">
      <b>This is a PREVIEW!  Hit "back" on your browser to go back to the editor,
      or hit "Save" if you're happy with what you see.</b>
   </div>

   <p><hr /></p>

   <form action="<wiki:EditLink format="url" />" method="POST" 
         ACCEPT-CHARSET="<wiki:ContentEncoding />">
   <p>

   <%-- These are required parts of this form.  If you do not include these,
        horrible things will happen.  Do not modify them either. --%>

   <input type="hidden" name="page"     value="<wiki:PageName/>" />
   <input type="hidden" name="action"   value="save" />
   <input type="hidden" name="edittime" value="<%=pageContext.getAttribute("lastchange", PageContext.REQUEST_SCOPE )%>" />
   <textarea rows="4" cols="20" readonly="true" style="display:none" name="text"><%=pageContext.getAttribute("usertext", PageContext.REQUEST_SCOPE) %></textarea>

   <div id="previewsavebutton" align="center">
      <input type="button" name="edit" value="Keep editing" onClick="javascript:back(-1);"/>
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      <input type="submit" name="ok" value="Save" />
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      <input type="submit" name="cancel" value="Cancel" />
   </div>

   </p>
   </form>
