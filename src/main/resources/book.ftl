<#-- generate page breaks -->
<#assign page=0>
<#macro pagebreak currpage=-1>
<#if currpage &gt;= 0>
	<#assign page=currpage />
<#else>
	<#assign page++ />
</#if>
<span epub:type="pagebreak" id="page${page}" title="${page}" xmlns:epub="http://www.idpf.org/2007/ops"/></#macro>

<#-- generate link to content -->
<#macro link filename>
<a href="${resolve(filename)}"><#nested></a></#macro>

<#-- generate reference to footnote -->
<#macro refnote note linktext=note file="99_footnotes.xhtml">
<a class="refnote" id="rn${note}" href="${resolve(file)}#fn${note}">${linktext}</a></#macro>

<#-- generate footnote with back reference -->
<#macro footnote note file="" linktext="↑">
<div class="footnote" id="fn${note}"><a href="${resolve(file)}#rn${note}">${linktext}</a> <#nested></div>
</#macro>

<#-- generate empty lines -->
<#macro spacer lines=1>
<div style="white-space: pre;"><#list 1..lines as x>
</#list>
</div>
</#macro>

