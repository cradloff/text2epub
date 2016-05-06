<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>
<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='${property.language}'>
  <head>
    <title>${property.title} - ${property.author}</title>
    <link href='${stylesheet}' type='text/css' rel='stylesheet'/>
  </head>
  <body class='toc'>
    <div id='author'>${property.author}</div>
    <div id='title'>${property.title}</div>
    <#if property.subtitle != "">
    <div id='subtitle'>${property.subtitle}</div>
    </#if>
    <h2>${resource.toc}</h2>
    <#macro printEntries entries>
    <#if entries?size &gt; 0>
    <ul>
    <#list entries as entry>
      <li>
        <a href='${entry.filename}'>${entry.title}</a>
        <@printEntries entries=entry.subEntries/>
      </li>
    </#list>
    </ul>
    </#if>
    </#macro>
    <@printEntries entries=tocEntries/>
  </body>
</html>
