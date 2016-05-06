package text2epub;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/** Klasse zum Verarbeiten von Templates mit FreeMarker */
public class FreeMarker {
	/** Zeichensatz */
	private static final String ENCODING = "UTF-8";
	/** FreeMarker-Konfiguration */
	private Configuration fmCfg;
	private ZipWriter writer;
	private Object data;

	/**
	 * Konstruktor.
	 * @param basedir Basis-Verzeichnis für Templates
	 */
	public FreeMarker(File basedir, ZipWriter writer, Object data) throws IOException {
		this.writer = writer;
		this.data = data;

		fmCfg = new Configuration(Configuration.VERSION_2_3_24);
		// Templates werden zuerst im Basis-Verzeichnis gesucht, dann im Classpath
		FileTemplateLoader ftl1 = new FileTemplateLoader(basedir);
		ClassTemplateLoader ctl = new ClassTemplateLoader(getClass(), "/");
		MultiTemplateLoader mtl = new MultiTemplateLoader(new TemplateLoader[] { ftl1, ctl });
		fmCfg.setTemplateLoader(mtl);
		fmCfg.setDefaultEncoding(ENCODING);
		fmCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	/** Schreibt ein FreeMarker-Template in die Zip-Datei */
	public void writeTemplate(String templateName, String path) throws IOException {
		writer.newEntry(path);
		writeTemplate(templateName, writer);
	}

	/** Führt ein FreeMarker-Template aus, und liefert das Ergebnis zurück */
	public String applyTemplate(String templateName) throws IOException {
		StringWriter out = new StringWriter();
		writeTemplate(templateName, out);

		return out.toString();
	}

	private void writeTemplate(String templateName, Writer out) throws IOException {
		Template template = fmCfg.getTemplate(templateName);
		try {
			template.process(data, out);
		} catch (TemplateException e) {
			throw new RuntimeException(e);
		}
	}
}
