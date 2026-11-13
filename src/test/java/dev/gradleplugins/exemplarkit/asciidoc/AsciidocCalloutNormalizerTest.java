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


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static dev.gradleplugins.exemplarkit.asciidoc.AsciidocCalloutNormalizer.normalize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class AsciidocCalloutNormalizerTest {
	@ParameterizedTest
	@ValueSource(strings = {"// <1>", "// (2)", "# <3>", "# (4)"})
	void removesAsciidocCalloutOfSingleLine(String callout) {
		assertThat(normalize("ls " + callout), equalTo("ls"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"// <5>", "// (6)", "# <7>", "# (8)"})
	void removesAsciidocCalloutOfMultipleLine(String callout) {
		assertThat(
			normalize("""
				Foo
				Bar %s
				Far
				""".formatted(callout)),
			equalTo("""
				Foo
				Bar
				Far
				"""));
	}

	@ParameterizedTest
	@ValueSource(strings = {"// <9>", "// (1)", "# <2>", "# (3)"})
	void removesAsciidocCalloutWithTailingWhitespace(String callout) {
		assertThat(normalize("./gradlew assemble " + callout + "  "), equalTo("./gradlew assemble"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"  // <4>", "   // (5)", "    # <6>", "     # (7)"})
	void removesAsciidocCalloutWithoutWhitespaceBetweenTextAnnotation(String callout) {
		assertThat(normalize("mkdir -p src/main/cpp" + callout), equalTo("mkdir -p src/main/cpp"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"//<8>", "//(9)", "#<1>", "#(2)"})
	void removesAsciidocCalloutWithoutWhitespaceBetweenTextAndAnnotationChars(String callout) {
		assertThat(normalize("unzip -o foo.zip " + callout), equalTo("unzip -o foo.zip"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"//  <3>", "//   (4)", "#    <5>", "#     (6)"})
	void removesAsciidocCalloutWithMoreThanOneSpaceBetweenTextAndAnnotationChars(String callout) {
		assertThat(normalize("tree . " + callout), equalTo("tree ."));
	}

	@ParameterizedTest
	@ValueSource(strings = {"// <7>", "//  (8)", " #  <9> ", "  #  (1)  "})
	void ignoreNonEndingPatternLikeNotAtTheEnd(String callout) {
		assertThat(normalize("sed -e '{" + callout + "}'"), equalTo("sed -e '{" + callout + "}'"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"// <10>", "// (213)", "# <4563>", "# (98732)"})
	void removesAsciidocCalloutOfWithMultipleDigits(String callout) {
		assertThat(normalize("cmd /c msvc.bat " + callout), equalTo("cmd /c msvc.bat"));
	}
}
