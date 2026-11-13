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

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.gradleplugins.exemplarkit.ExemplarExecutor.defaultExecutor;
import static dev.gradleplugins.exemplarkit.ExemplarRunner.create;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ExemplarRunnerTest {
	@TempDir
	protected File testDirectory;

	@Test
	void canExecuteEmptyExemplar() {
		Exemplar exemplar = Exemplar.builder().build();
		ExemplarExecutionResult result = create(defaultExecutor()).inDirectory(testDirectory).using(exemplar).run();
		assertThat(result.getStepResults(), Matchers.emptyIterable());
	}

	@Nested
	class StepOnSampleFromDirectory {
		@TempDir protected File testDirectory;

		ExemplarExecutionResult result;

		@BeforeEach
		void anExemplar() throws IOException {
			File sampleDirectory = new File(testDirectory, "sample");
			sampleDirectory.mkdir();
			Files.writeString(new File(sampleDirectory, "foo.txt").toPath(), """
				In a galaxy far far away,
				Tada, da da ta da ta
				Tada da da
				""", StandardOpenOption.CREATE, StandardOpenOption.APPEND);

			File workingDirectory = new File(testDirectory, "working-dir");
			workingDirectory.mkdir();

			Exemplar exemplar = Exemplar.builder()
				.fromDirectory(sampleDirectory)
				.step(Step.builder().execute("cat", "foo.txt"))
				.build();
			result = create(defaultExecutor()).inDirectory(workingDirectory).using(exemplar).run();
		}

		@Test
		void hasOneStepResult() {
			assertThat(result.getStepResults(), Matchers.iterableWithSize(1));
		}

		@Test
		void hasStepOutput() {
			assertThat(result.getStepResults().get(0).getOutput(), optionalWithValue());
			assertThat(result.getStepResults().get(0).getOutput().get(), equalTo("In a galaxy far far away,\nTada, da da ta da ta\nTada da da\n"));
		}

		@Test
		void hasExecutedOutcome() {
			assertThat(result.getStepResults().get(0).getOutcome(), equalTo(StepExecutionOutcome.EXECUTED));
		}

		@Test
		void hasExitValue() {
			assertThat(result.getStepResults().get(0).getExitValue(), optionalWithValue());
			assertThat(result.getStepResults().get(0).getExitValue().get(), equalTo(0));
		}
	}
}
