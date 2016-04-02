<?xml version='1.0' encoding='UTF-8'?>
<ncx xmlns='http://www.daisy.org/z3986/2005/ncx/' version='2005-1' xml:lang='${property.language}'>
  <head>
    <#--  Dieselbe Buchidentifikation wie in der OPF-Datei: -->
    <meta name='dtb:uid' content='${UUID}'/>
  </head>
  <docTitle>
    <text>${property.title}</text>
  </docTitle>
  <docAuthor>
    <text>${property.author}</text>
  </docAuthor>
  <navMap>
    <#assign order = 1 >
    <#-- Cover und Index-Seite auf oberster Ebene -->
    <#if params.COVER ??>
    <navPoint playOrder='${order}' id='navPoint-${order}'>
    <#assign order = order + 1 >
      <navLabel>
        <text>${resource.cover}</text>
      </navLabel>
      <content src='${params.COVER}'/>
    </navPoint>
    </#if>
    <#if params.TOC ??>
    <navPoint playOrder='${order}' id='navPoint-${order}'>
    <#assign order = order + 1 >
      <navLabel>
        <text>${property.title}</text>
      </navLabel>
      <content src='${params.TOC}'/>
      <#-- untergeordnet die Kapitel -->
      <#list tocEntries as entry>
      <navPoint playOrder='${order}' id='navPoint-${order}'>
      <#assign order = order + 1 >
        <navLabel>
          <text>${entry.title}</text>
        </navLabel>
        <content src='${entry.filename}'/>
      </navPoint>
      </#list>
    </navPoint>
    <#else>
    <#-- sonst die Kapitel auch auf oberster Ebene -->
    <#list tocEntries as entry>
    <navPoint playOrder='${order}' id='navPoint-${order}'>
    <#assign order = order + 1 >
      <navLabel>
        <text>${entry.title}</text>
      </navLabel>
      <content src='${entry.filename}'/>
    </navPoint>
    </#list>
    </#if>
  </navMap>
</ncx>
