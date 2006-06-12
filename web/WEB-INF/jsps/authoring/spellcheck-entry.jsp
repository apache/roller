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
<!-- spellcheck-entry.jsp -->
<%@ include file="/taglibs.jsp" %>
<%@ page import="org.apache.roller.model.RollerSpellCheck,com.swabunga.spell.event.SpellCheckEvent" %>
<%@ page import="org.apache.commons.lang.StringUtils,java.util.*" %>

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

<html:form action="/roller-ui/authoring/weblog" method="post">
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

    