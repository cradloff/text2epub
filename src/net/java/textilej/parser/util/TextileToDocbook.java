package net.java.textilej.parser.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import net.java.textilej.parser.markup.textile.TextileDialect;

public class TextileToDocbook extends MarkupToDocbook {


	public static void main(String[] args) {
		try {
			if (args.length < 1) {
				usage();
				System.exit(-1);
			}
			File inputFile = new File(args[0]);
			File outputFile = args.length < 2?null:new File(args[1]);
			if (outputFile != null && outputFile.exists()) {
				System.err.println("File "+outputFile+" already exists");
				usage();
				System.exit(-1);
			}
			
			if (!inputFile.exists()) {
				System.err.println("File "+outputFile+" does not exist");
				usage();
				System.exit(-1);
			}
			
			String textileSource = readFully(inputFile);
			
			TextileToDocbook textileToDocbook = new TextileToDocbook();
			String name = inputFile.getName();
			if (name.lastIndexOf('.') != -1) {
				name = name.substring(0,name.lastIndexOf('.'));
			}
			textileToDocbook.setBookTitle(name);
			
			String docbookSource = textileToDocbook.parse(textileSource);
			if (outputFile == null) {
				System.out.println(docbookSource);
			} else {
				Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile),"utf-8");
				try {
					writer.write(docbookSource);
				} finally {
					writer.close();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public TextileToDocbook() {
		setDialect(new TextileDialect());
	}
	
	private static String readFully(File inputFile) throws IOException {
		int length = (int) inputFile.length();
		if (length <= 0) {
			length = 2048;
		}
		StringBuilder buf = new StringBuilder(length);
		Reader reader = new BufferedReader(new FileReader(inputFile));
		try {
			int c;
			while ((c = reader.read()) != -1) {
				buf.append((char)c);
			}
		} finally {
			reader.close();
		}
		return buf.toString();
	}

	private static void usage() {
		System.err.println("Usage: java "+TextileToDocbook.class.getName()+" <input file> [output file]");
	}
	
}
