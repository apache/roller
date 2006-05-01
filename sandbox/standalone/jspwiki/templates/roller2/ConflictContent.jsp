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
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

   <div class="conflictnote">
      <p><b>Oops!  Someone modified the page while you were editing it!</b></p>

      <p>Since I am stupid and can't figure out what the difference
      between those pages is, you will need to do that for me.  I've
      printed here the text (in Wiki) of the new page, and the
      modifications you made.  You'll now need to copy the text onto a
      scratch pad (Notepad or emacs will do just fine), and then edit
      the page again.</p>

      <p>Note that when you go back into the editing mode, someone might have
      changed the page again.  So be quick.</p>

   </div>

      <p><font color="#0000FF">Here is the modified text (by someone else):</font></p>

      <p><hr /></p>

      <tt>
        <%=pageContext.getAttribute("conflicttext",PageContext.REQUEST_SCOPE)%>
      </tt>      

      <p><hr /></p>

      <p><font color="#0000FF">And here's your text:</font></p>

      <tt>
        <%=pageContext.getAttribute("usertext",PageContext.REQUEST_SCOPE)%>
      </tt>

      <p><hr /></p>

      <p>
       <i>Go edit <wiki:EditLink><wiki:PageName /></wiki:EditLink>.</i>
      </p>
