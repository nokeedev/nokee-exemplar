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

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static dev.gradleplugins.exemplarkit.asciidoc.AsciidoctorContent.load;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

final class AsciidoctorContentTest {
	@TempDir
	protected Path testDirectory;
	private final CollectingVisitor visitor = new CollectingVisitor();

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

	@Test
	void canVisitEmptyFile() {
		load(asciidoc()).walk(visitor);
		assertThat(visitor.nodes, contains("{document}"));
	}

	@Test
	void canVisitFileWithSingleHeader() {
		load(asciidoc("= Title\n")).walk(visitor);
		assertThat(visitor.nodes, contains("{document}"));
	}

	@Test
	void canVisitFileWithListingBlock() {
		File adocFile = asciidoc("""
			= Title

			[listing.terminal]
			----
			$ ./gradlew assemble

			BUILD SUCCESSFUL
			4 actionable tasks: 4 executed
			----
			""");

		load(adocFile).walk(visitor);
		assertThat(visitor.nodes, contains("{document}", "{block-listing}"));
	}

	@Test
	void canVisitFileWithMultiLanguageSample() {
		File adocFile = asciidoc("""
			= Title

			====
			[.multi-language-sample]
			=====
			.build.gradle
			[source,groovy]
			----
			plugins {
			    id 'dev.nokee.cpp-application'
			}
			----
			=====
			[.multi-language-sample]
			=====
			.build.gradle.kts
			[source,kotlin]
			----
			plugins {
			    id("dev.nokee.cpp-application")
			}
			----
			=====
			====
			""");

		load(adocFile).walk(visitor);
		assertThat(visitor.nodes, contains("{document}", "{block-example}", "{block-example}", "{block-listing}", "{block-example}", "{block-listing}"));
	}

	private static final class CollectingVisitor implements AsciidoctorContent.Visitor {
		final List<String> nodes = new ArrayList<>();

		@Override
		public void visit(Document node) {
			nodes.add("{document}");
		}

		@Override
		public void visit(Block node) {
			nodes.add("{block-" + node.getNodeName() + "}");
		}
	}
}
