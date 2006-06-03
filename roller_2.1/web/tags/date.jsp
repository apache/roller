<%@ include file="/taglibs.jsp" %><% {
String prefix = org.roller.presentation.tags.DateTag.KEY_PREFIX;
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
