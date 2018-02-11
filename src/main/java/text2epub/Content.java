package text2epub;

import java.io.File;

import text2epub.converter.Converter;

public class Content {
	private File file;
	private Converter converter;
	private String outputFilename;

	public Content(File file, String outputFilename, Converter converter) {
		super();
		this.file = file;
		this.outputFilename = outputFilename;
		this.converter = converter;
	}

	public File getFile() {
		return file;
	}

	public Converter getConverter() {
		return converter;
	}

	public String getOutputFilename() {
		return outputFilename;
	}
}
