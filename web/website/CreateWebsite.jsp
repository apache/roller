<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>
<script type="text/javascript">
<!--
function previewImage(theme) {
    <% String ctxPath = request.getContextPath(); %>
    document.preview.src="<%= ctxPath %>/images/preview/sm-theme-" + theme + ".png";
}
-->
</script>

<h1><fmt:message key="createWebsite.title" /></h1>

<br /> 
<html:form action="/editor/createWebsite" method="post" focus="handle">

    <div class="formrow">
       <label for="name" class="formrow" />
           <fmt:message key="createWebsite.name" /></label>
       <html:text property="name" size="30" maxlength="30" />
    </div>
    
    <div class="formrow">
       <label for="description" class="formrow" />
           <fmt:message key="createWebsite.description" /></label>
       <html:text property="description" size="30" maxlength="30" />
    </div>   
     
    <div class="formrow">
       <label for="handle" class="formrow" />
           <fmt:message key="createWebsite.handle" /></label>
       <html:text property="handle" size="30" maxlength="30" />
    </div>
    
    <div class="formrow">
       <label for="emailAddress" class="formrow" />
           <fmt:message key="createWebsite.emailAddress" /></label>
       <html:text property="emailAddress" size="30" maxlength="30" />
    </div>
    
    <div class="formrow">
       <label for="locale" class="formrow" />
           <fmt:message key="createWebsite.locale" /></label>
       <html:select property="locale" size="1" >
          <html:options collection="locales" property="value" labelProperty="label"/>
       </html:select>
    </div>
    
    <div class="formrow">
       <label for="timeZone" class="formrow" />
           <fmt:message key="createWebsite.timeZone" /></label>
       <html:select property="timeZone" size="1" >
           <html:options collection="timeZones" property="value" labelProperty="label"/>
       </html:select>
    </div>
    
    <div class="formrow">
       <label for="theme" class="formrow" />
           <fmt:message key="createWebsite.theme" /></label>
       <html:select property="theme" size="1" onchange="previewImage(this[selectedIndex].value)">
           <html:options name="model" property="themes" />
       </html:select>
    </div>
    
    <div class="formrow">
       <label for="preview" class="formrow" />&nbsp;</label>
       <img name="preview" 
          src="<%= request.getContextPath() %>/images/preview/sm-theme-basic.png" height="268" width="322" />
    </div>    
    
    <div class="control">
       <input type="submit" value='<fmt:message key="createWebsite.button.save" />'></input>
    </div>
    
</html:form>

<%@ include file="/theme/footer.jsp" %>


