package text2epub;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

/** Klasse zum Verarbeiten von Templates mit FreeMarker */
public class FreeMarker {
	private static final Version VERSION = Configuration.VERSION_2_3_24;
	/** Zeichensatz */
	private static final String ENCODING = "UTF-8";
	/** globale Include-Datei */
	private static final String AUTO_INCLUDE = "book.ftl";
	/** FreeMarker-Konfiguration */
	private Configuration fmCfg;
	private ZipWriter writer;
	private Object data;

	/**
	 * Konstruktor.
	 */
	public FreeMarker(ZipWriter writer, Object data) {
		this.writer = writer;
		this.data = data;
	}

	/**
	 * Erzeugt eine Konfiguration, die Resourcen aus dem Basis-Verzeichnis oder
	 * dem Classpath lädt.
	 * @param basedir Basis-Verzeichnis für Templates
	 * @throws IOException
	 */
	public void configure(File basedir) throws IOException {
		Configuration config = new Configuration(VERSION);
		// Templates werden zuerst im Basis-Verzeichnis gesucht, danach in den übergeordneten Verzeichnissen, dann im Classpath
		List<TemplateLoader> loader = new ArrayList<>();
		File dir = basedir.getCanonicalFile();
		do {
			loader.add(new FileTemplateLoader(dir));
			dir = dir.getParentFile();
		} while (dir != null);
		loader.add(new ClassTemplateLoader(getClass(), "/"));
		MultiTemplateLoader mtl = new MultiTemplateLoader(loader.toArray(new TemplateLoader[loader.size()]));
		config.setTemplateLoader(mtl);
		config.setDefaultEncoding(ENCODING);
		config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		setConfiguration(config);
	}

	/**
	 * Erzeugt eine Konfiguration, die Resourcen aus dem Classpath lädt.
	 */
	public void configureCP() {
		Configuration config = createConfiguration();
		config.setTemplateLoader(new ClassTemplateLoader(getClass(), "/"));

		setConfiguration(config);
	}

	/**
	 * Erzeugt eine Default-Konfiguration (ohne Template-Loader).
	 * @return Konfiguration
	 */
	public static Configuration createConfiguration() {
		Configuration config = new Configuration(VERSION);
		config.setDefaultEncoding(ENCODING);
		config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		// globales Freemarker-Include einbinden
		config.addAutoInclude(AUTO_INCLUDE);

		return config;
	}

	public void setConfiguration(Configuration fmCfg) {
		this.fmCfg = fmCfg;
	}

	/** Schreibt ein FreeMarker-Template in die Zip-Datei */
	public void writeTemplate(String templateName, String path) throws IOException {
		writer.newEntry(path);
		writeTemplate(templateName, writer);
	}

	/**
	 * Führt ein FreeMarker-Template aus, und liefert das Ergebnis zurück.
	 * @param file Datei
	 * @return Ergebnis des Templates
	 */
	public String applyTemplate(File file) throws IOException {
		return applyTemplate(file.getName());
	}

	/**
	 * Führt ein FreeMarker-Template aus, und liefert das Ergebnis zurück
	 * @param templateName Dateiname des Templates
	 * @return Ergebnis des Templates
	 */
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
