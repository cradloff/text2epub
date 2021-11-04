package text2epub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Testet {@link TocEntry} */
public class TocEntryTest {
	@Test public void constructor() {
		TocEntry entry = new TocEntry("h1", "title h1", "file h1");
		assertThat(entry)
			.hasFieldOrPropertyWithValue("level", "h1")
			.hasFieldOrPropertyWithValue("title", "title h1")
			.hasFieldOrPropertyWithValue("filename", "file h1");
		assertThat(entry.getSubEntries())
			.isEmpty();
	}

	@Test public void add() {
		// Root-Eintrag
		TocEntry root = new TocEntry("h0", "root", "n/a");
		// Sub-Entries hinzufügen
		root.add(new TocEntry("h1", "first", "n/a"));
		root.add(new TocEntry("h1", "second", "n/a"));
		root.add(new TocEntry("h2", "third", "n/a"));
		root.add(new TocEntry("h2", "fourth", "n/a"));
		root.add(new TocEntry("h3", "fifth", "n/a"));
		root.add(new TocEntry("h2", "sixth", "n/a"));
		root.add(new TocEntry("h1", "seventh", "n/a"));
		root.add(new TocEntry("h2", "eighth", "n/a"));
		root.add(new TocEntry("h3", "nineth", "n/a"));
		root.add(new TocEntry("h3", "tenth", "n/a"));
		// jetzt haben wir folgende Struktur:
		// root
		// |\+ first
		//   | second
		//   |\+ third
		//   | | fourth
		//   | |\+ fifth
		//   | | sixth
		//   + seventh
		//   |\+ eighth
		//   | |\+ nineth
		//   | | | tenth

		// erste Ebene
		assertEquals(3, root.getSubEntries().size());
		TocEntry first = root.getSubEntries().get(0);
		assertEquals("first", first.getTitle());
		TocEntry second = root.getSubEntries().get(1);
		assertEquals("second", second.getTitle());
		TocEntry seventh = root.getSubEntries().get(2);
		assertEquals("seventh", seventh.getTitle());

		// zweite Ebene
		assertEquals(0, first.getSubEntries().size());
		assertEquals(3, second.getSubEntries().size());
		TocEntry third = second.getSubEntries().get(0);
		assertEquals("third", third.getTitle());
		TocEntry fourth = second.getSubEntries().get(1);
		assertEquals("fourth", fourth.getTitle());
		TocEntry sixth = second.getSubEntries().get(2);
		assertEquals("sixth", sixth.getTitle());

		assertEquals(1, seventh.getSubEntries().size());
		TocEntry eighth = seventh.getSubEntries().get(0);
		assertEquals("eighth", eighth.getTitle());

		// dritte Ebene
		assertEquals(1, fourth.getSubEntries().size());
		TocEntry fifth = fourth.getSubEntries().get(0);
		assertEquals("fifth", fifth.getTitle());
		assertEquals(0, sixth.getSubEntries().size());

		assertEquals(2, eighth.getSubEntries().size());
		TocEntry nineth = eighth.getSubEntries().get(0);
		assertEquals("nineth", nineth.getTitle());
		TocEntry tenth = eighth.getSubEntries().get(1);
		assertEquals("tenth", tenth.getTitle());
	}
}
