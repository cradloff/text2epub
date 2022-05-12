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
<#assign currnote=0>
<#macro refnote note=-1 linktext="" file="99_footnotes.xhtml">
<#if note &gt;= 0>
	<#assign currnote=note />
<#else>
	<#assign currnote++ />
</#if>
<#if "" + linktext == "">
	<#assign mylinktext=currnote />
<#else>
	<#assign mylinktext=linktext />
</#if>
<a class="refnote" id="rn${currnote}" href="${resolve(file)}#fn${currnote}">${mylinktext}</a></#macro>

<#-- generate footnote with back reference -->
<#macro footnote note=-1 file="" linktext="↑">
<#if note &gt;= 0>
	<#assign currnote=note />
<#else>
	<#assign currnote++ />
</#if>
<div class="footnote" id="fn${currnote}"><a href="${resolve(file)}#rn${currnote}">${linktext}</a> <#nested></div>
</#macro>

<#-- generate empty lines -->
<#macro spacer lines=1>
<div style="white-space: pre;"><#list 1..lines as x>
</#list>
</div>
</#macro>

