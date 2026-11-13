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

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.gradleplugins.exemplarkit.Step.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class StepBuilderTest {
	@Test
	void throwsExceptionIfNoExecutableWasSpecified() {
		IllegalStateException ex = assertThrows(IllegalStateException.class, () -> builder().build());
		assertThat(ex.getMessage(), equalTo("Please specify an executable for this step."));
	}

	@Nested
	class Equals {
		private static Step step(UnaryOperator<Step.Builder> closure) {
			Step.Builder builder = builder();
			closure.apply(builder);
			return builder.build();
		}

		@Test
		void canCompareSteps() {
			new EqualsTester()
				.addEqualityGroup(
					step(it -> it.execute("cat")),
					step(it -> it.execute("cat")))
				.addEqualityGroup(step(it -> it.execute("ls")))
				.addEqualityGroup(
					step(it -> it.execute("ls", "-l")),
					step(it -> it.execute("ls", "-l")))
				.addEqualityGroup(
					step(it -> it.execute("cat", "bar.txt")),
					step(it -> it.execute("cat", "bar.txt")))
				.addEqualityGroup(
					step(it -> it.execute("ls").output("foo")),
					step(it -> it.execute("ls").output("foo")))
				.addEqualityGroup(step(it -> it.execute("ls").output("bar")))
				.addEqualityGroup(step(it -> it.execute("cat").output("foo")))
				.addEqualityGroup(
					step(it -> it.execute("cat").attribute("k1", "v1")),
					step(it -> it.execute("cat").attribute("k1", "v1")))
				.testEquals();
		}
	}

	@Nested
	class InstanceIntegrity {
		static Step step = builder().execute("ls", "-a").build();

		@Test
		void cannotModifyReturnedArguments() {
			assertThrows(UnsupportedOperationException.class, () -> step.getArguments().add("foo"));
			assertThrows(UnsupportedOperationException.class, () -> step.getArguments().clear());
			assertThrows(UnsupportedOperationException.class, () -> step.getArguments().remove(0));
		}
	}

	@Nested
	class ExecutableOnlyStep {
		static Step step = builder().execute("ls").build();

		@Test
		void hasExecutable() {
			assertThat(step.getExecutable(), equalTo("ls"));
		}

		@Test
		void noOutput() {
			assertThat(step.getOutput(), emptyOptional());
		}

		@Test
		void noAttributes() {
			assertThat(step.getAttributes(), anEmptyMap());
		}

		@Test
		void noArguments() {
			assertThat(step.getArguments(), empty());
		}
	}

	@Nested
	class CommandLineBuilderSafety {
		@Test
		void throwsExceptionWhenExecutableIsNull() {
			NullPointerException ex = assertThrows(NullPointerException.class, () -> builder().execute(null));
			assertThat(ex.getMessage(), equalTo("Executable cannot be null."));
		}

		@Test
		void throwsExceptionIfAnyArgumentsAreNull() {
			assertThrows(NullPointerException.class, () -> builder().execute("ls", (String) null));
			assertThrows(NullPointerException.class, () -> builder().execute("ls", "-l", null));

			NullPointerException ex = assertThrows(NullPointerException.class, () -> builder().execute("list", (String) null));
			assertThat(ex.getMessage(), equalTo("No arguments can be null."));
		}

		@Test
		void replaceExecutableAndArgumentsOnMultipleExecuteInvocation() {
			Step step = builder().execute("ls", "foo").execute("cat", "bar").build();
			assertThat(step.getExecutable(), equalTo("cat"));
			assertThat(step.getArguments(), contains("bar"));
		}

		@Test
		void stripWhitespaceBeforeExecutable() {
			Step step = builder().execute("  dir").build();
			assertThat(step.getExecutable(), equalTo("dir"));
		}

		@Test
		void stripWhitespaceAfterExecutable() {
			Step step = builder().execute("echo   ").build();
			assertThat(step.getExecutable(), equalTo("echo"));
		}

		@Test
		void stripWhitespaceAroundArguments() {
			Step step = builder().execute("echo", "  Hello, world!  ", " > ", " /path/with space/ ").build();
			assertThat(step.getArguments(), contains("Hello, world!", ">", "/path/with space/"));
		}
	}

	@Nested
	class CanAttachArgumentsToStep {
		Step step = builder().execute("ls", "-a", "/bin").build();

		@Test
		void hasExecutable() {
			assertThat(step.getExecutable(), equalTo("ls"));
		}

		@Test
		void noOutput() {
			assertThat(step.getOutput(), emptyOptional());
		}

		@Test
		void noAttributes() {
			assertThat(step.getAttributes(), anEmptyMap());
		}

		@Test
		void hasArguments() {
			assertThat(step.getArguments(), contains("-a", "/bin"));
		}
	}

	@Nested
	class CanAttachOutputToStep {
		Step.Builder stepBuilder = builder().execute("ls", "-l", "/bin");

		@Test
		void fromAListOfIndividualLines() {
			List<String> lines = Arrays.asList("total 4848",
						 "-rwxr-xr-x  1 root  wheel    35840 27 May 20:27 [",
						 "-r-xr-xr-x  1 root  wheel   623472 27 May 20:27 bash",
						 "-rwxr-xr-x  1 root  wheel    36768 27 May 20:27 cat",
						 "-rwxr-xr-x  1 root  wheel    47264 27 May 20:27 chmod",
						 "-rwxr-xr-x  1 root  wheel    42272 27 May 20:27 cp");
			Step step = stepBuilder.output(lines.get(0), lines.get(1), lines.subList(2, lines.size()).toArray(new String[0])).build();

			assertThat(step.getOutput(), optionalWithValue());
			assertThat(step.getOutput().get(), equalTo(String.join(System.lineSeparator(), lines)));
		}

		@Test
		void fromAMultiLineString() {
			String output = """
				total 4848
				-rwxr-xr-x  1 root  wheel   529424 27 May 20:27 csh
				rwxr-xr-x  1 root  wheel   110848 27 May 20:27 dash
				rwxr-xr-x  1 root  wheel    41872 27 May 20:27 date
				rwxr-xr-x  1 root  wheel    45120 27 May 20:27 dd
				rwxr-xr-x  1 root  wheel    36512 27 May 20:27 df""";
			Step step = stepBuilder.output(output).build();

			assertThat(step.getOutput(), optionalWithValue());
			assertThat(step.getOutput().get(), equalTo(output));
		}

		@Test
		void canRemovePreviousOutputByUsingNull() {
			Step step = stepBuilder.output("foo").output(null).build();
			assertThat(step.getOutput(), emptyOptional());
		}

		@Test
		void throwsAnExceptionIfAnyLinesAreNull() {
			assertThrows(NullPointerException.class, () -> stepBuilder.output("foo", null));
			assertThrows(NullPointerException.class, () -> stepBuilder.output(null, "foo"));
			assertThrows(NullPointerException.class, () -> stepBuilder.output("foo", "bar", (String) null));
			assertThrows(NullPointerException.class, () -> stepBuilder.output("foo", "bar", "far", null));

			NullPointerException ex = assertThrows(NullPointerException.class, () -> stepBuilder.output("foo", "bar", (String) null));
			assertThat(ex.getMessage(), equalTo("No output lines can be null."));
		}
	}

	@Nested
	class CanAttachAttributesToStep {
		Step.Builder stepBuilder = builder().execute("cat", "/foo/bar");

		@Test
		void attachSingleAttribute() {
			Step step = stepBuilder.attribute("k1", "v1").build();
			assertThat(step.getAttributes(), aMapWithSize(1));
			assertThat(step.getAttributes(), hasEntry("k1", "v1"));
		}

		@Test
		void attachMultipleAttributesIndividually() {
			Step step = stepBuilder.attribute("k2", "v2").attribute("k3", "v3").build();

			assertThat(step.getAttributes(), aMapWithSize(2));
			assertThat(step.getAttributes(), hasEntry("k2", "v2"));
			assertThat(step.getAttributes(), hasEntry("k3", "v3"));
		}

		@Test
		void attachMultipleAttributes() {
			Step step = stepBuilder.attributes(Map.of("k6", "v6", "k7", "v7")).build();

			assertThat(step.getAttributes(), aMapWithSize(2));
			assertThat(step.getAttributes(), hasEntry("k6", "v6"));
			assertThat(step.getAttributes(), hasEntry("k7", "v7"));
		}

		@Test
		void canReplaceAttributes() {
			Step step = stepBuilder.attribute("k2", "v2").attribute("k3", "v3").attributes(Map.of("k4", "v4")).build();

			assertThat(step.getAttributes(), aMapWithSize(1));
			assertThat(step.getAttributes(), hasEntry("k4", "v4"));
		}
	}

	@Nested
	class ToString {
		private static String step(UnaryOperator<Step.Builder> closure) {
			Step.Builder builder = builder();
			closure.apply(builder);
			return builder.build().toString();
		}

		@Test
		void executableOnly() {
			assertThat(step(it -> it.execute("ls")), equalTo("Step{command=ls}"));
		}

		@Test
		void executableWithArguments() {
			assertThat(step(it -> it.execute("cat", "/some/file")), equalTo("Step{command=cat /some/file}"));
		}

		@Test
		void executableWithMultiLineOutput() {
			assertThat(step(it -> it.execute("./foo").output("foo", "bar", "far")), equalTo("Step{command=./foo, output=foo[...]}"));
			assertThat(step(it -> it.execute("./foo").output("foo\r\nbar\r\nfar")), equalTo("Step{command=./foo, output=foo[...]}"));
			assertThat(step(it -> it.execute("./foo").output("foo\nbar\nfar")), equalTo("Step{command=./foo, output=foo[...]}"));
		}

		@Test
		void executableWithSingleLineOutput() {
			assertThat(step(it -> it.execute("./bar").output("bar")), equalTo("Step{command=./bar, output=bar}"));
		}

		@Test
		void executableWithAttributes() {
			assertThat(step(it -> it.execute("./far").attribute("k0", "v0").attribute("k1", "v1").attribute("k2", "v2")), equalTo("Step{command=./far, attributes={k0=v0, k1=v1, k2=v2}}"));
		}
	}
}
