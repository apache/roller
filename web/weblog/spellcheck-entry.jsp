<!-- spellcheck-entry.jsp -->
<%@ include file="/taglibs.jsp" %>
<%@ page import="org.roller.model.RollerSpellCheck,com.swabunga.spell.event.SpellCheckEvent" %>
<%@ page import="org.apache.commons.lang.StringUtils,java.util.*" %>
<%@ include file="/theme/header.jsp" %>
<script type="text/javascript">
<!--
    function postWeblogEntry(activeForm,publish)
    {
        if (publish)
            activeForm.publishEntry.value = "true";
        activeForm.submit();
    }
    
    function spellcheck(activeForm)
    {
        activeForm.method.value = "spellCheck";
        postWeblogEntry(activeForm);
    }
// -->
</script>

<roller:StatusMessage/>

<html:form action="/editor/weblog" method="post">
    <bean:define id="text" name="weblogEntryFormEx" 
          property="text" type="java.lang.String" />
    <input type="hidden" name="method" value="spellCheck"/>
    <input type="hidden" name="publishEntry" value="false" />
    <html:hidden property="title" />
    <html:hidden property="categoryId" />
    <html:hidden property="dateString" />
    <html:hidden property="hours" />
    <html:hidden property="minutes" />
    <html:hidden property="seconds" />
    <html:hidden property="day"/>
    <html:hidden property="id"/>
    <html:hidden property="anchor"/>
<h3>Use Spell Check Results</h3>

    <table class="rTable" width="90%" border="1">
        <tr class="rEvenTr">
            <td class="rTd"><font style='font-size: 14px; vertical-align: middle; line-height= 18px; font-family: verdana, sans-serif;'><%= escapeText %></font></td>
        </tr>
    </table>
    <br />
    <input type="button" name="post" value="Post to Weblog" 
            onClick="javascript:postWeblogEntry(this.form,true)" />
    &nbsp; &nbsp;
    <input type="button" name="draft" value="Save as Draft" 
            onClick="javascript:postWeblogEntry(this.form)" />      

</html:form>

<h2>Or</h2>

<html:form action="/weblog" method="post" focus="title">

<h3>Edit Manually</h3>
    <input type="hidden" name="method" value="update"/>
    <input type="hidden" name="publishEntry" value="false" />
    <html:hidden property="title" />
    <html:hidden property="categoryId" />
    <html:hidden property="dateString" />
    <html:hidden property="hours" />
    <html:hidden property="minutes" />
    <html:hidden property="seconds" />
    <html:hidden property="day"/>
    <html:hidden property="id"/>
    <html:hidden property="anchor"/>
    <html:textarea property="text" cols="75" rows="20" style="width: 90%"/> 
    <br />
    <br />
    <input type="button" name="post" value="Post to Weblog" 
            onClick="javascript:postWeblogEntry(this.form, true)" />
    &nbsp; &nbsp;
    <input type="button" name="draft" value="Save as Draft" 
            onClick="javascript:postWeblogEntry(this.form)" />      
    &nbsp; &nbsp;
    <input type="button" name="spelling" value="Recheck" 
            onClick="javascript:spellcheck(this.form)" />  

</html:form>

    
<%@ include file="/theme/footer.jsp" %>