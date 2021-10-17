package text2epub;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class MetaDataScannerTest {
	@Test
	public void parseDirectoryName() {
		assertThat(MetaDataScanner.parseDirectoryName("Das leere Haus"))
				.containsEntry("title", "Das leere Haus")
				.containsEntry("author", "")
				.containsEntry("authorFileAs", "")
				.containsEntry("groupPosition", "");

		assertThat(MetaDataScanner.parseDirectoryName("Das leere Haus - Arthur Conan Doyle"))
				.containsEntry("title", "Das leere Haus")
				.containsEntry("author", "Arthur Conan Doyle")
				.containsEntry("authorFileAs", "Doyle, Arthur Conan")
				.containsEntry("groupPosition", "");

		assertThat(MetaDataScanner.parseDirectoryName("Das leere Haus - Doyle, Arthur Conan"))
				.containsEntry("title", "Das leere Haus")
				.containsEntry("author", "Arthur Conan Doyle")
				.containsEntry("authorFileAs", "Doyle, Arthur Conan")
				.containsEntry("groupPosition", "");

		assertThat(MetaDataScanner.parseDirectoryName("Das leere Haus - Doyle"))
				.containsEntry("title", "Das leere Haus")
				.containsEntry("author", "Doyle")
				.containsEntry("authorFileAs", "")
				.containsEntry("groupPosition", "");

		assertThat(MetaDataScanner.parseDirectoryName("10 - Das leere Haus"))
				.containsEntry("title", "Das leere Haus")
				.containsEntry("author", "")
				.containsEntry("authorFileAs", "")
				.containsEntry("groupPosition", "10");

		assertThat(MetaDataScanner.parseDirectoryName("10 - Das leere Haus - Arthur Conan Doyle"))
				.containsEntry("title", "Das leere Haus")
				.containsEntry("author", "Arthur Conan Doyle")
				.containsEntry("authorFileAs", "Doyle, Arthur Conan")
				.containsEntry("groupPosition", "10");

		assertThat(MetaDataScanner.parseDirectoryName("10_Das leere Haus_Arthur Conan Doyle"))
				.containsEntry("title", "Das leere Haus")
				.containsEntry("author", "Arthur Conan Doyle")
				.containsEntry("authorFileAs", "Doyle, Arthur Conan")
				.containsEntry("groupPosition", "10");

		assertThat(MetaDataScanner.parseDirectoryName("10_Das leere Haus_Doyle, Arthur Conan"))
				.containsEntry("title", "Das leere Haus")
				.containsEntry("author", "Arthur Conan Doyle")
				.containsEntry("authorFileAs", "Doyle, Arthur Conan")
				.containsEntry("groupPosition", "10");

		assertThat(MetaDataScanner.parseDirectoryName("10_Das leere Haus_Doyle"))
				.containsEntry("title", "Das leere Haus")
				.containsEntry("author", "Doyle")
				.containsEntry("authorFileAs", "")
				.containsEntry("groupPosition", "10");
	}
}
