# Text to EPUB converter

`text2epub` is a simple but powerful tool to convert text to ebooks in EPUB 
format. It converts text from the following formats:

* [Markdown](https://daringfireball.net/projects/markdown/) (\*.txt, \*.md)
* [Textile](https://txstyle.org/) (*.textile)
* Wiki / [MediaWiki](https://www.mediawiki.org/wiki/MediaWiki) (*.wiki)
* [Trac](https://trac.edgewall.org/) (*.trac)
* [Confluence](https://www.atlassian.com/software/confluence) (*.confluence)
* [Asciidoc](http://asciidoctor.org/) (*.adoc)

If your content is already in XHTML-Format (*.xhtml), it will also be included.

When first called, it creates a template `epub.xml` and exits. You just fill out
this file with the metadata (author, title, etc.) and the next time you call
`text2epub`, it will scan for content and create a ebook from it. The files are 
added alphabetically, so you should add a prefix to sort it (e.g. `01_index.txt`, 
`02_chapter_one.txt`, ...). The cover has to be named `cover.jpg` (or 
`cover.png`, `cover.gif` or `cover.svg`) and will be embedded in a html file.

## Stylesheet
You can provide a stylesheet for the complete book or for each content file. The 
global stylesheet must be named `book.css`, the other stylesheets 
`<basename>.css`. If you don't provide a stylesheet, a default one is embedded.

## Table of contents
A table of contents is created by default. `text2epub` scans for header tags, 
default is `h1`, other tags can be specified in the properties.

## Internationalization
`text2epub` has builtin support for english and german. To add support for 
another language, extract the file `Text2Epub_en.properties` and rename it to 
match you language (e.g. `Text2Epub_es.properties` for spanish). Put that file 
in the directory where the jar archive is installed. Please send me your 
translation so I can include it.

## Scripting
`text2epub` uses [FreeMarker](https://freemarker.apache.org/) to create the ebook.
You can also use it for scripting. Just place your script inside the content and
`text2epub` will execute it before converting to XHTML. To disable that 
feature, set the `freemarker` property to `false`. There are already some
predefined macros you can use:

<dl>
<dt>pagebreak</dt>
<dd>This macro can be used to place pagebreaks in you ebook, these are references to 
actual page numbers in the printed version of the book. The first call in each file 
has to specify the starting page (e.g. &lt;@pagebreak 1/&gt;) in the following calls 
the page number is automatically increased an can be omitted (&lt;@pagebreak&gt;). 
If there is at least one pagebreak, <code>text2epub</code> 
creates a pagemap for the book.</dd>

<dt>refnote</dt>
<dd>Creates a link to a footnote in another file (default <code>99_footnotes.xhtml</code>).
The macro has three parameters:<dl>
<dt>note</dt><dd>the id of the footnote (required, e.g. <code>1</code>)</dd>
<dt>linktext</dt><dd>the text shown in the link (default is note)</dd>
<dt>file</dt><dd>filename containing footnotes (default <code>99_footnotes.xhtml</code>). 
The filename is automatically resolved, i.e. you can specify the filename of the source file 
(e.g. chapter.md) which will be replaced with the filename in the ebook (chapter.xhtml).</dd>
</dl>
Example: <code>&lt;@refnote 1 "*"/&gt;</code></dd>

<dt>footnote</dt>
<dd>Creates a footnote with a back link to the refnote entry. The macro has three parameters:<dl>
<dt>note</dt><dd>id of the footnote (required, e.g. <code>1</code>)</dd>
<dt>file</dt><dd>filename which contains the reference (default current file)</dd>
<dt>linktext</dt><dd>the text shown in the link (default <code>↑</code>)</dd>
</dl>
Example: <code>&lt;@footnote 1 "chapter01.md" "^"&gt;Here is the text&lt;/@footnote&gt;</code></dd>

<dt>spacer</dt>
<dd>Creates an empty line. If specified, more lines can be created: 
<code>&lt;@spacer 3/&gt;</code> creates three empty lines</dd>

<dt>resolve(srcFilename)</dt>
<dd>Resolves the given filename of a source file and returns the filename in the ebook. If `srcFilename`
is not known, it will be returned. Example: <code>${resolve("chapter.md")}</code></dd>
</dl>

You can access the properties from `epub.xml` with the expression 
`${property.<name>}` e.g. `${property.title}` for the book title.

## Customization
Most files are created from FreeMarker templates which are contained in the jar 
archive. If you want to change these templates, just extract them from the jar and
put them in the local directory. Here you can customize them as you wish.

Resources (but not content!) are searched in the following locations:
1. First they are searched in the local directory where your content resides
2. Next, all parent directories are scanned
3. Last the classpath is used

So, if you want to customize a certain resource for all of your books, put the
corresponding template in a common base directory or in the same directory where 
the jar archive is installed.

## Prerequisites
`text2epub` is written in Java, so you have to install a JRE (version 7 or higher).
The JRE can be downloaded from here: 
[https://java.com/en/download/](https://java.com/en/download/)

## Installation
Just unzip the Jar and the shell script somewhere where it can be accessed. You can 
put it in its own directory or in an existing one. Add this directory to your 
`PATH` variable.

Thats all!

## Usage
The shell script has two optional parameters:
1. Directory containing the content. Defaults to current working directory.
2. Output file name. Defaults to the filename specified in `epub.xml`, if 
not specified `"<Title> - <Author>.epub"` is used.

