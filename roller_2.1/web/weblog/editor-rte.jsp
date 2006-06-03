
<%-- This page is designed to be included in edit-weblog.jsp --%>

<%@ include file="/taglibs.jsp" %>

<html:hidden property="text" />
<script type="text/javascript" src="richtext.js" ></script>
<script type="text/javascript" src="html2xhtml.js" ></script>
<script type="text/javascript">
<!--
    function postWeblogEntry(publish)
    {
	updateRTE('rte1');
        document.weblogEntryFormEx.text.value = document.weblogEntryFormEx.rte1.value;
        if (publish) document.weblogEntryFormEx.publishEntry.value = "true";
        document.weblogEntryFormEx.submit();
    }
   //Usage: initRTE(imagesPath, includesPath, cssFile, genXHTML)
   initRTE("images/", "<%= request.getContextPath() %>/editor/", "", true);
//-->
</script>
<noscript><p><b>Javascript must be enabled to use this form.</b></p></noscript>


<br />
<script language="JavaScript" type="text/javascript">
<!--
//Usage: writeRichText(fieldname, html, width, height, buttons, readOnly)
writeRichText('rte1', document.weblogEntryFormEx.text.value, '95%', 300, true, false);
//-->
</script>

        <script type="text/javascript">
            <!--
            if (getCookie("editorSize") != null) {
                document.weblogEntryFormEx.text.rows = getCookie("editorSize");
            }
            -->
        </script>
       <div style="float:right">
          <script type="text/javascript">
            <!--
            function changeSize(e,num) {
                e.form.text.rows = e.form.text.rows + num;
                var expires = new Date();
                expires.setTime(expires.getTime() + 24 * 90 * 60 * 60 * 1000); // sets it for approx 90 days.
                setCookie("editorSize",e.form.rte1.rows,expires);
            }
            -->
          </script>
          <!-- Add buttons to make this textarea taller or shorter
          <input type="button" name="taller" value=" &darr; " onclick="changeSize(this,5)" />
          <input type="button" name="shorter" value=" &uarr; " onclick="changeSize(this,-5)" />
           -->
       </div>