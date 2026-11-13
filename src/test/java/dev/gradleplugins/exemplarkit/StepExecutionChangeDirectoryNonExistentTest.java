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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static dev.gradleplugins.exemplarkit.StepExecutors.changeDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

class StepExecutionChangeDirectoryNonExistentTest {
	@TempDir
	protected static File testDirectory;
	static StepExecutionResult result;
	static StepExecutionContext context;

	@BeforeAll
	static void changeToNonExistentDirectory() {
		context = new StepExecutionContext(testDirectory).forStep(cd("foo"));
		result = changeDirectory().run(context);
	}

	private static Step cd(String path) {
		return Step.builder().execute("cd", path).build();
	}

	@Test
	void hasExecutedOutcome() {
		assertThat(result.getOutcome(), equalTo(StepExecutionOutcome.EXECUTED));
	}

	@Test
	void hasNonZeroExitValue() {
		assertThat(result.getExitValue().get(), not(equalTo(0)));
	}

	@Test
	void doesNotChangeCurrentWorkingDirectory() throws IOException {
		assertThat(context.getCurrentWorkingDirectory().getCanonicalFile(), equalTo(testDirectory.getCanonicalFile()));
	}
}
