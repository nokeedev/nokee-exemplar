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
package dev.gradleplugins.exemplarkit.output;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class JarCommandOutput {
	private final Set<String> paths;
	private final String output;

	private JarCommandOutput(Set<String> paths, String output) {
		this.paths = paths;
		this.output = output;
	}

	public String get() {
		return output;
	}

	public static JarCommandOutput from(String output) {
		dev.gradleplugins.exemplarkit.output.JarCommandOutputLexer lexer = new dev.gradleplugins.exemplarkit.output.JarCommandOutputLexer(CharStreams.fromString(output));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		dev.gradleplugins.exemplarkit.output.JarCommandOutputParser parser = new dev.gradleplugins.exemplarkit.output.JarCommandOutputParser(tokens);

		ExtractJarModel l = new ExtractJarModel();
		ParseTreeWalker.DEFAULT.walk((ParseTreeListener)l, parser.output());

		return new JarCommandOutput(l.allPaths, output);
	}

	// Equality only considers the parsed paths, not the raw output
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof JarCommandOutput)) return false;

		return Objects.equals(paths, ((JarCommandOutput) o).paths);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(paths);
	}

	private static class ExtractJarModel extends dev.gradleplugins.exemplarkit.output.JarCommandOutputBaseListener {
		Set<String> allPaths = new HashSet<>();
		@Override
		public void enterPath(dev.gradleplugins.exemplarkit.output.JarCommandOutputParser.PathContext ctx) {
			allPaths.add(ctx.getText());
		}
	}
}
