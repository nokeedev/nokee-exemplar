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
import java.util.ArrayList;
import java.util.List;

import static dev.gradleplugins.exemplarkit.ExemplarRunner.create;
import static dev.gradleplugins.exemplarkit.StepExecutionResult.stepSkipped;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

class ExemplarRunnerConfigurationTest {
	private static final File aWorkingDirectory = Paths.get("/a/working/directory").toFile();
	private static final Exemplar exemplar = Exemplar.builder().build();
	private final RecordingStepExecutor defaultStepExecutor = new RecordingStepExecutor();
	private final ExemplarExecutor executor = ExemplarExecutor.builder().defaultCommandLineToolExecutor(defaultStepExecutor).build();

	@Nested
	class WorkingDirectoryConfiguration {
		final ExemplarRunner exemplarRunner = create(executor).inDirectory(aWorkingDirectory);

		@Test
		void returnNonNullInstance() {
			assertThat(exemplarRunner, notNullValue());
		}

		@Test
		void canQueryWorkingDirectoryFromRunner() {
			assertThat(exemplarRunner.getWorkingDirectory(), equalTo(aWorkingDirectory));
		}

		@Test
		void canOverwriteWorkingDirectory() {
			File anotherWorkingDirectory = Paths.get("/another/working/directory").toFile();
			assertThat(exemplarRunner.inDirectory(anotherWorkingDirectory).getWorkingDirectory(), equalTo(anotherWorkingDirectory));
		}

		@Test
		void throwsExceptionIfWorkingDirectoryIsNull() {
			NullPointerException ex = assertThrows(NullPointerException.class, () -> exemplarRunner.inDirectory(null));
			assertThat(ex.getMessage(), equalTo("Please specify a non-null working directory."));
		}
	}

	@Nested
	class ExemplarConfiguration {
		@Test
		void returnNonNullInstance() {
			assertThat(create(executor).using(exemplar), notNullValue());
		}

		@Test
		void throwsExceptionIfExemplarIsNull() {
			NullPointerException ex = assertThrows(NullPointerException.class, () -> create(executor).using(null));
			assertThat(ex.getMessage(), equalTo("Please specify a non-null exemplar."));
		}
	}

	@Test
	void throwsExceptionIfRunningWithoutWorkingDirectory() {
		InvalidRunnerConfigurationException ex = assertThrows(InvalidRunnerConfigurationException.class, () -> create(executor).using(exemplar).run());
		assertThat(ex.getMessage(), equalTo("Please specify a working directory using ExemplarRunner#inDirectory(File)."));
		assertThat(defaultStepExecutor.runs, empty());
	}

	@Test
	void throwsExceptionIfRunningWithoutExemplar() {
		InvalidRunnerConfigurationException ex = assertThrows(InvalidRunnerConfigurationException.class, () -> create(executor).inDirectory(aWorkingDirectory).run());
		assertThat(ex.getMessage(), equalTo("Please specify an exemplar using ExemplarRunner#using(Exemplar)."));
		assertThat(defaultStepExecutor.runs, empty());
	}

	private static final class RecordingStepExecutor implements StepExecutor {
		final List<StepExecutionContext> runs = new ArrayList<>();

		@Override
		public boolean canHandle(Step step) {
			return true;
		}

		@Override
		public StepExecutionResult run(StepExecutionContext context) {
			runs.add(context);
			return stepSkipped();
		}
	}
}
