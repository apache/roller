<%@ taglib uri="http://java.sun.com/jstl/xml" prefix="x" %>
<%@ include file="/taglibs.jsp" %>
<%@ include file="/theme/header.jsp" %>

<%-- Start by parsing our config defs using the jstl xml toolkit --%>
<%-- Then we'll progress through the config defs and print out the form --%>
<x:parse var="configDefs">
  <%= org.roller.config.RollerRuntimeConfig.getRuntimeConfigDefsAsString() %>
</x:parse>


<roller:StatusMessage/>

<h1><fmt:message key="configForm.title" /></h1>
<form action="rollerConfig.do" method="post">

<input type="hidden" name="method" value="update">

    <table class="formtableNoDesc">
    
    <x:forEach select="$configDefs//config-def[@name='global-properties']/display-group">
        <c:set var="displayGroupKey"><x:out select="@key"/></c:set>
    
        <tr>
            <td colspan="3"><h2><fmt:message key="${displayGroupKey}" /></h2></td>
        </tr>
    
        <x:forEach select="property-def">
            <c:set var="propLabelKey"><x:out select="@key"/></c:set>
            <c:set var="name"><x:out select="@name"/></c:set>
        
            <tr>
                <td class="label"><fmt:message key="${propLabelKey}" /></td>
              
                <%-- choose the right html input element for the display --%>
                <x:choose>
                
                  <%-- "string" type means use a simple textbox --%>
                  <x:when select="type='string'">
                    <td class="field"><input type="text" name='<c:out value="${name}"/>' value='<c:out value="${RollerProps[name].value}"/>' size="35" /></td>
                  </x:when>
                  
                  <%-- "text" type means use a full textarea --%>
                  <x:when select="type='text'">
                    <td class="field">
                      <textarea name='<c:out value="${name}"/>' rows="<x:out select="rows"/>" cols="<x:out select="cols"/>"><c:out value="${RollerProps[name].value}"/></textarea>
                    </td>
                  </x:when>
                  
                  <%-- "boolean" type means use a checkbox --%>
                  <x:when select="type='boolean'">
                    <c:choose>
                      <c:when test="${RollerProps[name].value eq 'true'}">
                          <td class="field"><input type="checkbox" name='<c:out value="${name}"/>' CHECKED></td>
                      </c:when>
                      <c:otherwise>
                          <td class="field"><input type="checkbox" name='<c:out value="${name}"/>'></td>
                      </c:otherwise>
                    </c:choose>
                  </x:when>
                  
                  <%-- if it's something we don't understand then use textbox --%>
                  <x:otherwise>
                    <td class="field"><input type="text" name='<c:out value="${name}"/>' size="50" /></td>
                  </x:otherwise>
                </x:choose>
                
                <td class="description"><%-- <fmt:message key="" /> --%></td>
            </tr>
          
        </x:forEach>
      
        <tr>
            <td colspan="2">&nbsp;</td>
        </tr>
        
    </x:forEach>

    </table>
    
    <div class="control">
        <input class="buttonBox" type="submit" value="<fmt:message key="configForm.save"/>"/>
    </div>
    
<form>


<%@ include file="/theme/footer.jsp" %>