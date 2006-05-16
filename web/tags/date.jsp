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
<%@ include file="/taglibs.jsp" %><% {
String prefix = org.apache.roller.ui.core.tags.DateTag.KEY_PREFIX;
String formName = request.getAttribute(prefix+"_formName").toString(); 
String dateField = request.getAttribute(prefix+"_property").toString(); 
String dateFormat = request.getAttribute(prefix+"_dateFormat").toString();
String value = request.getAttribute(prefix+"_value").toString();
Boolean readOnly = (Boolean)request.getAttribute(prefix+"_readOnly");
%>
<script type="text/javascript" >
<!--
if (document.layers) { // Netscape 4 hack
    var cal<%= dateField %> = new CalendarPopup();
} else {
    var cal<%= dateField %> = new CalendarPopup("datetagdiv");
    document.write(cal<%= dateField %>.getStyles());
}
// -->
</script>
<input size="12" type="text" name="<%= dateField %>" id="<%= dateField %>"
    value="<%= value %>" readonly="readonly" />      
<% if (!readOnly.booleanValue()) { %>
   <a href="#" id="anchor<%= dateField %>" name="anchor<%= dateField %>"
       onclick="cal<%= dateField %>.select(document.<%= formName %>.<%= dateField %>,'anchor<%= dateField %>','<%= dateFormat %>'); return false">
   <img src='<c:url value="/images/calendar.png"/>' class="calIcon" alt="Calendar" /> </a>
<% } 
}
%>
