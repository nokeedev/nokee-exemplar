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
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;

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
		assertThat(new ProcessBuilder("zip", "-r", archiveFile.getAbsolutePath(), "hello").directory(sampleDirectory).start().waitFor(), equalTo(0));

		// when:
		Sample.fromArchive(archiveFile).writeToDirectory(exampleDirectory);

		// then:
		assertThat(new File(exampleDirectory, "hello"), anExecutableFile());
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
		assertThat(Sample.empty().toString(), equalTo("Sample.empty()"));
	}

	@Test
	void sampleFromDirectoryToString(@TempDir File sampleDirectory) {
		assertThat(Sample.fromDirectory(sampleDirectory).toString(), equalTo("Sample.fromDirectory(" + sampleDirectory.getAbsolutePath() + ")"));
	}

	@Test
	void sampleFromArchiveToString(@TempDir File testDirectory) throws IOException {
		File archiveFile = new File(testDirectory, "bar.zip");
		archiveFile.createNewFile();
		assertThat(Sample.fromArchive(archiveFile).toString(), equalTo("Sample.fromArchive(" + archiveFile.getAbsolutePath() + ")"));
	}

	private static Matcher<File> anExecutableFile() {
		return new TypeSafeDiagnosingMatcher<File>() {
			@Override
			protected boolean matchesSafely(File actual, Description mismatchDescription) {
				if (!actual.canExecute()) {
					mismatchDescription.appendValue(actual).appendText(" is not executable");
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("an executable file");
			}
		};
	}
}
