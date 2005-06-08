
<%@ include file="/theme/header.jsp"%>

<div id="content" style="margin-top: 40px">

<!--ul>
<li> Unable to <a href="#copypaste">Copy-Paste</a> to/from the Ekit editor.</li>
</ul-->

<a name="copypaste"><h3>Unable to Copy-Paste to/from Ekit editor</h3>
	<div class="output">
	You can enable access to the clipboard from Java applets by editing the java.policy file in the JRE (Java Runtime Environment) lib/security directory. On Windows, this will be a file such as:<br /><br />

	C:\Program Files\Java\j2re1.4.1_01\lib\security\java.policy<br /><br />

	Edit this file and look for the general-purpose grant entry, which grants permissions to all applets. Add the following setting to this section. For example:<br /><br />

	grant {<br /><br />

	  // Leave existing settings..<br /><br />

	  // Try to renable the clipboard access.<br />
	  permission java.awt.AWTPermission "accessClipboard";<br />
	};
	</div>
	<author>Thanks to <a href="http://www.jroller.com/page/ericfj/20030320#fix_for_freeroller_ekit_applet">Eric Foster-Johnson</a>.</author>
</a>

</div>

<%@ include file="/theme/footer.jsp"%>