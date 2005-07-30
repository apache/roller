
<% try { %>
<script type="text/javascript">
<!--
function dirty() {
    messages = document.getElementById("messages");
    if (messages != null) {
	    messages.className = "warnings";
	    var n1 = messages.childNodes[0];
	    var n2 = document.createTextNode("Unsaved changes");
	    messages.replaceChild(n2, n1);
    }
}
-->
</script>

<%-- Error Messages --%>
<logic:messagesPresent>
    <div id="errors" class="errors">
        <html:messages id="error">
            <c:out value="${error}" /><br />
        </html:messages>
    </div>
</logic:messagesPresent>

<%-- Success Messages --%>
<logic:messagesPresent message="true">
    <div id="messages" class="messages">
        <html:messages id="message" message="true">
            <c:out value="${message}" escapeXml="false"/><br />
        </html:messages>
    </div>
</logic:messagesPresent>

<% } catch (Throwable e) { e.printStackTrace(); } %>
