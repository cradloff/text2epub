package text2epub;

import java.io.File;
import java.io.IOException;

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

		fmCfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_22);
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
		Template template = fmCfg.getTemplate(templateName);
		try {
			template.process(data, writer);
		} catch (TemplateException e) {
			throw new RuntimeException(e);
		}
	}
}
