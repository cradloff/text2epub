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
    <#-- Cover und Index-Seite auf oberster Ebene -->
    <navPoint playOrder='1' id='navPoint-1'>
      <navLabel>
        <text>${resource.cover}</text>
      </navLabel>
      <content src='${params.COVER}'/>
    </navPoint>");
    <navPoint playOrder='2' id='navPoint-2'>
      <navLabel>
        <text>${property.title}</text>
      </navLabel>
      <content src='${params.TOC}'/>
      <#-- untergeordnet die Kapitel -->
      <#list tocEntries as entry>
      <navPoint playOrder='${entry?index + 3}' id='navPoint-${entry?index + 3}'>
        <navLabel>
          <text>${entry.title}</text>
        </navLabel>
        <content src='${entry.filename}'/>
      </navPoint>
      </#list>
    </navPoint>
  </navMap>
</ncx>
