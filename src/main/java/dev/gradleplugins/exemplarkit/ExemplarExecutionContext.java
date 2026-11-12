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

import java.io.File;
import java.util.Objects;

final class ExemplarExecutionContext {
	private final File workingDirectory;
	private final Exemplar exemplar;

	ExemplarExecutionContext(File workingDirectory, Exemplar exemplar) {
		this.workingDirectory = workingDirectory;
		this.exemplar = exemplar;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public Exemplar getExemplar() {
		return exemplar;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ExemplarExecutionContext)) return false;

		ExemplarExecutionContext that = (ExemplarExecutionContext) o;
		return Objects.equals(workingDirectory, that.workingDirectory) && Objects.equals(exemplar, that.exemplar);
	}

	@Override
	public int hashCode() {
		return Objects.hash(workingDirectory, exemplar);
	}

	@Override
	public String toString() {
		return "ExemplarExecutionContext(workingDirectory=" + workingDirectory + ", exemplar=" + exemplar + ")";
	}
}
