/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.gradleplugins.exemplarkit;

import dev.gradleplugins.exemplarkit.fixtures.ExemplarSample;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SampleTest {
	@TempDir
	protected File testDirectory;

	@Test
	void canWriteEmptySampleToDisk() {
		Sample.empty().writeToDirectory(testDirectory);
		assertThat(testDirectory.listFiles(), Matchers.emptyArray());
	}

	@Test
	void canWriteDirectoryBaseSampleToDisk() {
		File sampleDirectory = writeSampleToDirectory();
		File exampleDirectory = new File(testDirectory, "example");

		// when:
		Sample.fromDirectory(sampleDirectory).writeToDirectory(exampleDirectory);

		// then:
		assertThat(exampleDirectory.list(), arrayContaining("hello"));
	}

	@Test
	void canWriteArchiveBaseSampleToDisk() {
		File sampleDirectory = writeSampleToDirectory();
		File exampleDirectory = new File(testDirectory, "example");
		File archiveFile = new File(testDirectory, "sample.zip");
		ZipUtil.pack(sampleDirectory, archiveFile);

		// when:
		Sample.fromArchive(archiveFile).writeToDirectory(exampleDirectory);

		// then:
		assertThat(exampleDirectory.list(), arrayContaining("hello"));
	}

	@Test
	@DisabledOnOs(value = { OS.WINDOWS }, disabledReason = "because Windows doesn't have an issue with executable permission")
	void restoreExecutablePermissionFromArchiveBaseSample() throws IOException, InterruptedException {
		File sampleDirectory = writeSampleToDirectory();
		File exampleDirectory = new File(testDirectory, "example");
		File archiveFile = new File(testDirectory, "sample.zip");
		assertEquals(0, new ProcessBuilder("zip", "-r", archiveFile.getAbsolutePath(), "hello").directory(sampleDirectory).start().waitFor());

		// when:
		Sample.fromArchive(archiveFile).writeToDirectory(exampleDirectory);

		// then:
		assertTrue(new File(exampleDirectory, "hello").canExecute());
	}

	private File writeSampleToDirectory() {
		ExemplarSample fixture = new ExemplarSample();
		File sampleDirectory = new File(testDirectory, "sample");
		fixture.writeToDirectory(sampleDirectory);
		new File(sampleDirectory, "hello").setExecutable(true);
		return sampleDirectory;
	}

	@Test
	void emptySampleToString() {
		assertEquals("Sample.empty()", Sample.empty().toString());
	}

	@Test
	void sampleFromDirectoryToString(@TempDir File sampleDirectory) {
		assertEquals("Sample.fromDirectory(" + sampleDirectory.getAbsolutePath() + ")", Sample.fromDirectory(sampleDirectory).toString());
	}

	@Test
	void sampleFromArchiveToString(@TempDir File testDirectory) throws IOException {
		File archiveFile = new File(testDirectory, "bar.zip");
		archiveFile.createNewFile();
		assertEquals("Sample.fromArchive(" + archiveFile.getAbsolutePath() + ")", Sample.fromArchive(archiveFile).toString());
	}
}
