/*
 * Copyright 2020 the original author or authors.
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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static dev.gradleplugins.exemplarkit.Exemplar.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExemplarBuilderTest {

	@Nested
	class Empty {
		static Exemplar subject = builder().build();

		@Test
		void hasNoSteps() {
			assertThat(subject.getSteps(), emptyIterable());
		}

		@Test
		void hasEmptySample() {
			assertThat(subject.getSample(), equalTo(Sample.empty()));
		}
	}

	@Nested
	class AddingSteps {
		private final Step newStep() {
			return Step.builder().execute("cat").build();
		}

		@Test
		void singleStep() {
			Step step = newStep();
			Exemplar exemplar = builder().step(step).build();
			assertThat(exemplar.getSteps(), contains(step));
		}

		@Test
		void multiStep() {
			Step step1 = newStep();
			Step step2 = newStep();
			Exemplar exemplar = builder().step(step1).step(step2).build();
			assertThat(exemplar.getSteps(), contains(step1, step2));
		}

		@Test
		void canUseStepBuilder() {
			Exemplar exemplar = builder().step(Step.builder().execute("ls")).build();
			assertThat(exemplar.getSteps(), iterableWithSize(1));
		}

		@Test
		void throwsExceptionForNullStep() {
			NullPointerException ex = assertThrows(NullPointerException.class, () -> builder().step((Step) null));
			assertEquals("Step cannot be null.", ex.getMessage());
		}

		@Test
		void throwsExceptionForNullStepBuilder() {
			NullPointerException ex = assertThrows(NullPointerException.class, () -> builder().step((Step.Builder) null));
			assertEquals("Step builder cannot be null.", ex.getMessage());
		}
	}

	@Test
	void canSourceSampleFromDirectory(@TempDir File testDirectory) {
		File sampleDirectory = testDirectory;
		Exemplar exemplar = builder().fromDirectory(sampleDirectory).build();
		assertEquals(Sample.fromDirectory(sampleDirectory), exemplar.getSample());
	}

	@Test
	void throwsExceptionIfSampleDirectoryDoesNotExists(@TempDir File testDirectory) {
		File sampleDirectory = new File(testDirectory, "non-existant");
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> builder().fromDirectory(sampleDirectory));
		assertEquals("Please specify a valid directory because directory '" + testDirectory.getAbsolutePath() + File.separator + "non-existant' does not exists.", ex.getMessage());
	}

	@Test
	void canSourceSampleFromArchive(@TempDir File testDirectory) throws IOException {
		File sampleArchive = new File(testDirectory, "foo.zip");
		sampleArchive.createNewFile();
		Exemplar exemplar = builder().fromArchive(sampleArchive).build();
		assertEquals(Sample.fromArchive(sampleArchive), exemplar.getSample());
	}

	@Test
	void throwsExceptionIfSampleArchiveDoesNotExists(@TempDir File testDirectory) {
		File sampleArchive = new File(testDirectory, "foo.zip");
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> builder().fromArchive(sampleArchive));
		assertEquals("Please specify a valid archive because archive '" + testDirectory.getAbsolutePath() + File.separator + "foo.zip' does not exists.", ex.getMessage());
	}
}
