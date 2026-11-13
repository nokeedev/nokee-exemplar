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
import java.io.IOException;

import static dev.gradleplugins.exemplarkit.StepExecutors.changeDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class StepExecutionChangeDirectoryTest {
	@TempDir
	protected File testDirectory;

	private static Step cd(String path) {
		return Step.builder().execute("cd", path).build();
	}

	@Test
	void failStepWhenCanonicalizeSandboxDirectory() {
		StepExecutionContext context = new StepExecutionContext(new ThrowExceptionWhenCanonicalizeFile(testDirectory))
			.currentWorkingDirectory(testDirectory)
			.forStep(cd("foo"));
		StepExecutionResult result = changeDirectory().run(context);
		assertThat(result.getOutcome(), equalTo(StepExecutionOutcome.FAILED));
		assertThat(result.getReason().orElse(null), equalTo("Could not canonicalize."));
	}

	private static class ThrowExceptionWhenCanonicalizeFile extends File {
		ThrowExceptionWhenCanonicalizeFile(File file) {
			super(file.getAbsolutePath());
		}

		@Override
		public File getCanonicalFile() throws IOException {
			throw new IOException("Could not canonicalize.");
		}
	}
}
