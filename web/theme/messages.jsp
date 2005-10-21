
<% try { %>

<%-- Error Messages --%>
<logic:messagesPresent>
    <div class="errors">
        <html:messages id="error">
            <c:out value="${error}" /><br />
        </html:messages>
    </div>
</logic:messagesPresent>

<%-- Success Messages --%>
<logic:messagesPresent message="true">
    <div class="messages">
        <html:messages id="message" message="true">
            <c:out value="${message}" escapeXml="false"/><br />
        </html:messages>
    </div>
</logic:messagesPresent>

<% } catch (Throwable e) { e.printStackTrace(); } %>
