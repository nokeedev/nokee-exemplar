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

import java.io.File;
import java.nio.file.Paths;

import static dev.gradleplugins.exemplarkit.ExemplarRunner.create;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExemplarRunnerConfigurationTest {
	private static final File aWorkingDirectory = Paths.get("/a/working/directory").toFile();
	private static final Exemplar exemplar = Exemplar.builder().build();
	private final StepExecutor defaultStepExecutor = mock(StepExecutor.class);
	private final ExemplarExecutor executor = ExemplarExecutor.builder().defaultCommandLineToolExecutor(defaultStepExecutor).build();

	@Nested
	class WorkingDirectoryConfiguration {
		final ExemplarRunner exemplarRunner = create(executor).inDirectory(aWorkingDirectory);

		@Test
		void returnNonNullInstance() {
			assertNotNull(exemplarRunner);
		}

		@Test
		void canQueryWorkingDirectoryFromRunner() {
			assertEquals(exemplarRunner.getWorkingDirectory(), aWorkingDirectory);
		}

		@Test
		void canOverwriteWorkingDirectory() {
			File anotherWorkingDirectory = Paths.get("/another/working/directory").toFile();
			assertEquals(exemplarRunner.inDirectory(anotherWorkingDirectory).getWorkingDirectory(), anotherWorkingDirectory);
		}

		@Test
		void throwsExceptionIfWorkingDirectoryIsNull() {
			NullPointerException ex = assertThrows(NullPointerException.class, () -> exemplarRunner.inDirectory(null));
			assertEquals("Please specify a non-null working directory.", ex.getMessage());
		}
	}

	@Nested
	class ExemplarConfiguration {
		@Test
		void returnNonNullInstance() {
			assertNotNull(create(executor).using(exemplar));
		}

		@Test
		void throwsExceptionIfExemplarIsNull() {
			NullPointerException ex = assertThrows(NullPointerException.class, () -> create(executor).using(null));
			assertEquals("Please specify a non-null exemplar.", ex.getMessage());
		}
	}

	@Test
	void throwsExceptionIfRunningWithoutWorkingDirectory() {
		InvalidRunnerConfigurationException ex = assertThrows(InvalidRunnerConfigurationException.class, () -> create(executor).using(exemplar).run());
		assertEquals("Please specify a working directory using ExemplarRunner#inDirectory(File).", ex.getMessage());
		verify(defaultStepExecutor, never()).run(any());
	}

	@Test
	void throwsExceptionIfRunningWithoutExemplar() {
		InvalidRunnerConfigurationException ex = assertThrows(InvalidRunnerConfigurationException.class, () -> create(executor).inDirectory(aWorkingDirectory).run());
		assertEquals("Please specify an exemplar using ExemplarRunner#using(Exemplar).", ex.getMessage());
		verify(defaultStepExecutor, never()).run(any());
	}
}
