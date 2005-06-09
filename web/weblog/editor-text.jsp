
<%-- This page is designed to be included in edit-weblog.jsp --%>

<%@ include file="/taglibs.jsp" %>

<script type="text/javascript">
<!--
function postWeblogEntry(publish)
{
    if (publish)
        document.weblogEntryFormEx.publishEntry.value = "true";
    document.weblogEntryFormEx.submit();
}
// -->
</script>

        <script type="text/javascript">
            <!--
            if (getCookie("editorSize") != null) {
                document.weblogEntryFormEx.text.rows = getCookie("editorSize");
            }
            -->
        </script>

<html:textarea property="text" cols="75" rows="20" style="width: 100%" tabindex="2"/>

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
                setCookie("editorSize",e.form.text.rows,expires);
            }
            -->
          </script>
          <!-- Add buttons to make this textarea taller or shorter -->
          <input type="button" name="taller" value=" &darr; " onclick="changeSize(this,5)" />
          <input type="button" name="shorter" value=" &uarr; " onclick="changeSize(this,-5)" />
       </div>

