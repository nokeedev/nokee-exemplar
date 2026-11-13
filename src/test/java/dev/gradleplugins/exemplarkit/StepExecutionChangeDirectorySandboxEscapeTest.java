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


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static dev.gradleplugins.exemplarkit.StepExecutors.changeDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StepExecutionChangeDirectorySandboxEscapeTest {
	private static Step cd(String path) {
		return Step.builder().execute("cd", path).build();
	}

	@Test
	void throwsExceptionWhenChangingDirectoryOutsideOfSandboxDirectoryRelatively(@TempDir File testDirectory) {
		StepExecutionContext context = new StepExecutionContext(testDirectory).forStep(cd(".."));
		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
			StepExecutionResult result = changeDirectory().run(context);
			assertThat(result.getOutcome(), equalTo(StepExecutionOutcome.FAILED));
			assertThat(result.getReason().orElse(null), equalTo("Cannot change directory outside of sandbox directory."));
		});
		assertThat(ex.getMessage(), equalTo("Cannot change directory outside of sandbox directory."));
	}

	@Test
	void throwsExceptionWhenChangingDirectoryOutsideOfSandboxDirectoryAbsolute(@TempDir File testDirectory) {
		StepExecutionContext context = new StepExecutionContext(testDirectory).forStep(cd(testDirectory.getParent()));
		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
			StepExecutionResult result = changeDirectory().run(context);
			assertThat(result.getOutcome(), equalTo(StepExecutionOutcome.FAILED));
			assertThat(result.getReason().orElse(null), equalTo("Cannot change directory outside of sandbox directory."));
		});
		assertThat(ex.getMessage(), equalTo("Cannot change directory outside of sandbox directory."));
	}

	@Test
	void doesNotThrowExceptionWhenChangingDirectoryBackToSandboxDirectoryRelatively(@TempDir File testDirectory) {
		// given:
		StepExecutionContext context = new StepExecutionContext(testDirectory)
			.currentWorkingDirectory(new File(testDirectory, "foo"))
			.forStep(cd(".."));
		StepExecutionResult result = changeDirectory().run(context);

		// expect:
		assertThat(result.getOutcome(), equalTo(StepExecutionOutcome.EXECUTED));
	}

	@Test
	void doesNotThrowExceptionWhenChangingDirectoryBackToSandboxDirectoryAbsolute(@TempDir File testDirectory) {
		// given:
		StepExecutionContext context = new StepExecutionContext(testDirectory)
			.currentWorkingDirectory(new File(testDirectory, "foo"))
			.forStep(cd(testDirectory.getAbsolutePath()));
		StepExecutionResult result = changeDirectory().run(context);

		// expect:
		assertThat(result.getOutcome(), equalTo(StepExecutionOutcome.EXECUTED));
	}
}
