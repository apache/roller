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
