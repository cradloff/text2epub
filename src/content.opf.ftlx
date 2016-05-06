<?xml version='1.0' encoding='UTF-8'?>
<package version='2.0'
  xmlns:dc='http://purl.org/dc/elements/1.1/'
  xmlns:opf='http://www.idpf.org/2007/opf'
  xmlns='http://www.idpf.org/2007/opf'
  unique-identifier='BookId'>
  <metadata>
    <dc:identifier id='BookId' opf:scheme='UUID'>${UUID}</dc:identifier>
    <#-- Hauptsprache des Buches -->
    <dc:language>${property.language}</dc:language>
    <#-- Buchtitel -->
    <dc:title xml:lang='${property.language}'>${property.title}</dc:title>
    <#-- bis hier notwendige Metainformationen, es folgen einige optionale: -->
    <#-- Beschreibung -->
    <#if property.description ?? && property.description != "">
    <dc:description xml:lang='${property.language}'>${property.description}</dc:description>
    </#if>
    <#-- Erzeuger, Erschaffer des digitalen Buches, hier auch der Autor -->
    <dc:creator
      <#if property.authorFileAs != "">
      opf:file-as='${property.authorFileAs}' opf:role='aut'
      <#else>
      opf:file-as='${property.author}' opf:role='aut'
      </#if>
      xml:lang='${property.language}'>${property.author}</dc:creator>
    <#if property.subject ?? && property.subject != "">
    <dc:subject>${property.subject}</dc:subject>
    </#if>
    <#if property.rights ?? && property.rights != "">
    <dc:rights>${property.rights}</dc:rights>
    </#if>
    <#-- Cover -->
    <#if params.COVER ??>
    <meta name='cover' content='${params.COVER_ID}'/>
    </#if>
    <#-- Charakteristischer Zeitpunkt der Erstellung des Buches -->
    <dc:date opf:event='creation'>${creation}</dc:date>
    <#-- Zeitpunkt der Veröffentlichung -->
    <#if property.pubDate != "">
    <dc:date opf:event='publication'>${property.pubDate}</dc:date>
    </#if>
  </metadata>
  <#-- Verzeichnis der Dateien des Buches -->
  <manifest>
    <item id='ncx' href='${params.NCX}' media-type='application/x-dtbncx+xml'/>
    <#list contentFiles as entry>
    <item id='${entry.id}' href='${entry.filename}' media-type='${entry.mimeType}'/>
    </#list>
    <#list mediaFiles as entry>
    <item id='${entry.id}' href='${entry.filename}' media-type='${entry.mimeType}'/>
    </#list>
  </manifest>
  <#-- Reihenfolge der Inhalte des Buches -->
  <spine toc='ncx'>
    <#list contentFiles as entry>
    <itemref idref='${entry.id}'/>
    </#list>
  </spine>
  <guide>
    <#if params.COVER ??>
    <reference type='cover' title='${resource.cover}' href='${params.COVER}'/>
    </#if>
    <#if params.TOC ??>
    <reference type='toc' title='${resource.toc}' href='${params.TOC}'/>
    </#if>
  </guide>
</package>
