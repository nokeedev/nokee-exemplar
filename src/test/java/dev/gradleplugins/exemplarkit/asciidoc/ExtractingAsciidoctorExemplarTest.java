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
package dev.gradleplugins.exemplarkit.asciidoc;

import dev.gradleplugins.exemplarkit.Step;
import dev.gradleplugins.exemplarkit.testers.StepTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static dev.gradleplugins.exemplarkit.asciidoc.AsciidocExemplarStepLoader.extractFromAsciiDoc;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;

class ExtractingAsciidoctorExemplarTest {
	@TempDir
	protected Path testDirectory;

	private File asciidoc() {
		return asciidoc("");
	}

	private File asciidoc(String content) {
		File adocFile = testDirectory.resolve("foo.adoc").toFile();
		try {
			Files.writeString(adocFile.toPath(), content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return adocFile;
	}

	@Nested
	class EmptyAsciidocFile {
		@Test
		void hasNoSteps() {
			assertThat(extractFromAsciiDoc(asciidoc()), emptyIterable());
		}
	}

	@Nested
	class SingleTerminalListingBlockWithSingleCommandAndNoOutput {
		List<Step> steps;

		@BeforeEach
		void anAdocFile() {
			File adocFile = asciidoc("""
				= Title

				[listing.terminal]
				----
				$ cd foo/bar
				----
				""");
			this.steps =  extractFromAsciiDoc(adocFile);
		}

		@Test
		void hasOneStep() {
			assertThat(steps.size(), equalTo(1));
		}

		@Nested
		class OnlyStep extends StepTester {
			@Override
			public Step getStepUnderTest() {
				return steps.get(0);
			}

			@Override
			public String getExpectedExecutable() {
				return "cd";
			}

			@Override
			public List<String> getExpectedArguments() {
				return Arrays.asList("foo/bar");
			}

			@Override
			public String getExpectedOutput() {
				return "";
			}

			@Override
			public Map<String, Object> getExpectedAttributes() {
				return Map.of("1", "listing", "style", "listing", "role", "terminal");
			}
		}
	}

	@Nested
	class SingleTerminalListingBlockWithMultipleCommand {
		List<Step> steps;

		@BeforeEach
		void anAdocFile() {
			File adocFile = asciidoc("""
				= Title

				[listing.terminal]
				----
				$ ls /usr/bin
				$ cat foo
				Some
				Text
				 * In
				 * Foo
				----
				""");
			this.steps =  extractFromAsciiDoc(adocFile);
		}

		@Test
		void hasTwoSteps() {
			assertThat(steps.size(), equalTo(2));
		}

		@Nested
		class FirstStep extends StepTester {
			@Override
			public Step getStepUnderTest() {
				return steps.get(0);
			}

			@Override
			public String getExpectedExecutable() {
				return "ls";
			}

			@Override
			public List<String> getExpectedArguments() {
				return Arrays.asList("/usr/bin");
			}

			@Override
			public String getExpectedOutput() {
				return "";
			}

			@Override
			public Map<String, Object> getExpectedAttributes() {
				return Map.of("1", "listing", "style", "listing", "role", "terminal");
			}
		}

		@Nested
		class SecondStep extends StepTester {
			@Override
			public Step getStepUnderTest() {
				return steps.get(1);
			}

			@Override
			public String getExpectedExecutable() {
				return "cat";
			}

			@Override
			public List<String> getExpectedArguments() {
				return Arrays.asList("foo");
			}

			@Override
			public String getExpectedOutput() {
				return """
					Some
					Text
					 * In
					 * Foo""";
			}

			@Override
			public Map<String, Object> getExpectedAttributes() {
				return Map.of("1", "listing", "style", "listing", "role", "terminal");
			}
		}
	}

	@Nested
	class SingleTerminalListingBlockWithSingleCommandAndOutput {
		List<Step> steps;

		@BeforeEach
		void anAdocFile() {
			File adocFile = asciidoc("""
				= Title

				[listing.terminal]
				----
				$ ./gradlew clean assemble

				BUILD SUCCESSFUL
				5 actionable tasks: 5 executed
				----
				""");
			this.steps =  extractFromAsciiDoc(adocFile);
		}

		@Test
		void hasOneStep() {
			assertThat(steps.size(), equalTo(1));
		}

		@Nested
		class FirstStep extends StepTester {
			@Override
			public Step getStepUnderTest() {
				return steps.get(0);
			}

			@Override
			public String getExpectedExecutable() {
				return "./gradlew";
			}

			@Override
			public List<String> getExpectedArguments() {
				return Arrays.asList("clean", "assemble");
			}

			@Override
			public String getExpectedOutput() {
				return """

					BUILD SUCCESSFUL
					5 actionable tasks: 5 executed""";
			}

			@Override
			public Map<String, Object> getExpectedAttributes() {
				return Map.of("1", "listing", "style", "listing", "role", "terminal");
			}
		}
	}

	@Nested
	class CommandLineAsciidocCalloutNormalizerTest {
		List<Step> steps;

		@BeforeEach
		void anAdocFile() {
			File adocFile = asciidoc("""
				= Title

				[listing.terminal]
				----
				$ ./gradlew clean assemble // <1>

				BUILD SUCCESSFUL
				5 actionable tasks: 5 executed
				----
				""");
			this.steps =  extractFromAsciiDoc(adocFile);
		}

		@Nested
		class FirstStep extends StepTester {
			@Override
			public Step getStepUnderTest() {
				return steps.get(0);
			}

			@Override
			public String getExpectedExecutable() {
				return "./gradlew";
			}

			@Override
			public List<String> getExpectedArguments() {
				return Arrays.asList("clean", "assemble");
			}

			@Override
			public String getExpectedOutput() {
				return """

					BUILD SUCCESSFUL
					5 actionable tasks: 5 executed""";
			}

			@Override
			public Map<String, Object> getExpectedAttributes() {
				return Map.of("1", "listing", "style", "listing", "role", "terminal");
			}
		}
	}

	@Nested
	class CommandOutputAsciidocCalloutNormalizerTest {
		List<Step> steps;

		@BeforeEach
		void anAdocFile() {
			File adocFile = asciidoc("""
				= Title

				[listing.terminal]
				----
				$ ./gradlew clean assemble

				BUILD SUCCESSFUL // (2)
				5 actionable tasks: 5 executed
				----
				""");
			this.steps =  extractFromAsciiDoc(adocFile);
		}

		@Nested
		class FirstStep extends StepTester {
			@Override
			public Step getStepUnderTest() {
				return steps.get(0);
			}

			@Override
			public String getExpectedExecutable() {
				return "./gradlew";
			}

			@Override
			public List<String> getExpectedArguments() {
				return Arrays.asList("clean", "assemble");
			}

			@Override
			public String getExpectedOutput() {
				return """

					BUILD SUCCESSFUL
					5 actionable tasks: 5 executed""";
			}

			@Override
			public Map<String, Object> getExpectedAttributes() {
				return Map.of("1", "listing", "style", "listing", "role", "terminal");
			}
		}
	}
}
