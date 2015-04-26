package org.markdown2epub;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.github.rjeschke.txtmark.Processor;

/**
 * Konvertiert Markdown-Dateien nach Epub.
 *
 * @author Claus Radloff
 */
public class Markdown2Epub {
	public static void main(String... args) throws IOException {
		File basedir = new File(args[0]);
		for (File file : basedir.listFiles()) {
			if (file.getName().endsWith(".md")) {
				convert(file, "Die Gespräche der Aloisia Sigaea", "book.css");
			}
		}
	}

	private static void convert(File file, String title, String css) throws IOException {
		String output = Processor.process(file);
		String outputFilename = file.getName();
		outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf(".md"));
		outputFilename += ".xhtml";
		File outputFile = new File(file.getParent(), outputFilename);
		try (PrintWriter out = new PrintWriter(outputFile)) {
			// Header ausgeben
			out.println("<?xml version='1.0' encoding='UTF-8'?>");
			out.println("<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.1//EN' 'http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd'>");
			out.println("<html xmlns='http://www.w3.org/1999/xhtml'>");
			out.println("<head>");
			out.println("<meta http-equiv='Content-Type' content='text/htmlcharset=utf-8'/>");
			out.printf("<title>%s</title>%n", title);
			out.printf("<link href='%s' type='text/css' rel='stylesheet'/>%n", css);
			out.println("</head>");
			out.println("<body>");

			// Inhalt
			out.write(output);

			// Footer
			out.println("</body>");
			out.println("</html>");
		}

		System.out.printf("Datei %s erstellt!%n", outputFilename);
	}
}
