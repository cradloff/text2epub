package text2epub.converter;

public interface Converter {
	/** Konvertiert die Eingabe nach XHtml */
	public String convert(String content);

	/** Liefert der Converter ein XHtml-Fragment zurück? */
	public boolean isFragment();

}
