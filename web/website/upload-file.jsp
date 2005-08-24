<%@ include file="/taglibs.jsp" %>

<roller:StatusMessage/>

<p class="subtitle">
    <fmt:message key="uploadFiles.subtitle" >
        <fmt:param value="${model.rollerSession.currentWebsite.handle}" />
    </fmt:message>
</p>  
<p class="pagetip">
    <fmt:message key="uploadFiles.tip" />
</p>

<roller:FileUpload />

<br />

<h1><fmt:message key="uploadFiles.manageFiles" /></h1>
    
<html:form action="/editor/uploadFiles" method="post">
    <roller:FileManager />
    <table>
       <tr>
          <td align="left">
             <input type="submit" value='<fmt:message key="uploadFiles.button.delete" />' /></input>
          </td>
       </tr>
    </table>
    <input type="hidden" name="method" value="delete"></input>
</html:form>

<%--
Added by Matt Raible since the focus javascript generated by Struts uses
a name reference and IE seems to only focus on file inputs via elements[0]?
--%>
<script type="text/javascript">
<!--
    document.forms[0].elements[0].focus();
// -->
</script>




