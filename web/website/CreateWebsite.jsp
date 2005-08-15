<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>
<script type="text/javascript">
<!--
function previewImage(theme) {
    <% String ctxPath = request.getContextPath(); %>
    document.preview.src="<%= ctxPath %>/images/preview/sm-theme-" + theme + ".png";
}
function cancel() {
    document.createWebsiteForm.method.value="cancel"; 
    document.createWebsiteForm.submit();
}
-->
</script>

<h1><fmt:message key="createWebsite.title" /></h1>
<p class="subtitle"><fmt:message key="createWebsite.prompt" /></p>

<br /> 
<html:form action="/editor/createWebsite" method="post" focus="handle">
<input type="hidden" name="method" ></input> 

<table class="formtable">

<tr>
    <td class="label"><label for="name" /><fmt:message key="createWebsite.name" /></label></td>
    <td class="field"><html:text property="name" size="30" maxlength="30" /></td>
    <td class="description"><fmt:message key="createWebsite.tip.name" /></td>
</tr>

<tr>
    <td class="label"><label for="description" /><fmt:message key="createWebsite.description" /></td>
    <td class="field"><html:text property="description" size="30" maxlength="30" /></td>
    <td class="description"><fmt:message key="createWebsite.tip.description" /></td>
</tr>

<tr>
    <td class="label"><label for="handle" /><fmt:message key="createWebsite.handle" /></label></td>
    <td class="field"><html:text property="handle" size="30" maxlength="30" /></td>
    <td class="description"><fmt:message key="createWebsite.tip.handle" /></td>
</tr>

<tr>
    <td class="label"><label for="emailAddress" /><fmt:message key="createWebsite.emailAddress" /></label></td>
    <td class="field"><html:text property="emailAddress" size="30" maxlength="30" /></td>
    <td class="description"><fmt:message key="createWebsite.tip.email" /></td>
</tr>

<tr>
    <td class="label"><label for="locale" /><fmt:message key="createWebsite.locale" /></label></td>
    <td class="field">
       <html:select property="locale" size="1" >
          <html:options collection="locales" property="value" labelProperty="label"/>
       </html:select>    
    </td>
    <td class="description"><fmt:message key="createWebsite.tip.locale" /></td>
</tr>

<tr>
    <td class="label"><label for="timeZone" /><fmt:message key="createWebsite.timeZone" /></label></td>
    <td class="field">
       <html:select property="timeZone" size="1" >
           <html:options collection="timeZones" property="value" labelProperty="label"/>
       </html:select>
    </td>
    <td class="description"><fmt:message key="createWebsite.tip.timezone" /></td>
</tr>

<tr>
    <td class="label"><label for="theme" /><fmt:message key="createWebsite.theme" /></label></td>
    <td class="field">
       <html:select property="theme" size="1" onchange="previewImage(this[selectedIndex].value)">
           <html:options name="model" property="themes" />
       </html:select>
       <br />
       <br />
       <img name="preview" src="<%= request.getContextPath() %>/images/preview/sm-theme-basic.png" height="268" width="322" />
    </td>
    <td class="description"><fmt:message key="createWebsite.tip.theme" /></td>
</tr>
</table>

<br />
   
<input type="submit" value='<fmt:message key="createWebsite.button.save" />'></input>
<input type="button" value='<fmt:message key="createWebsite.button.cancel" />' onclick="cancel()"></input>
    
</html:form>

<%@ include file="/theme/footer.jsp" %>


