<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
{
"Result":
[
    <s:iterator id="directory" value="childDirectories" status="dirStatus">
{"label":"<s:property value="#directory.name" />","key":"<s:property value="#directory.id" />","type":"dir"}<s:if test="(!#dirStatus.last) || (#dirStatus.last && childFiles.size() > 0)">,</s:if>
    </s:iterator>
    <s:iterator id="mediaFile" value="childFiles" status="fileStatus">
{"label":"<s:property value="#mediaFile.name" />","key":"<s:property value="#mediaFile.id" />","type":"file"}<s:if test="!#fileStatus.last">,</s:if>
    </s:iterator>
]
}
