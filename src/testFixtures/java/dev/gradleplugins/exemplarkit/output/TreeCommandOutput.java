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

import dev.gradleplugins.exemplarkit.output.TreeCommandOutputParser.EntryContext;
import dev.gradleplugins.exemplarkit.output.TreeCommandOutputParser.TreeHeaderContext;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public final class TreeCommandOutput {
	final List<String> paths;
	private final String output;

	public TreeCommandOutput(List<String> paths, String output) {
		this.paths = paths;
		this.output = output;
	}

	public String get() {
		return output;
	}

	public static TreeCommandOutput from(String output) {
		TreeCommandOutputLexer lexer = new TreeCommandOutputLexer(CharStreams.fromString(output));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		TreeCommandOutputParser parser = new TreeCommandOutputParser(tokens);


		ExtractTreeModel l = new ExtractTreeModel();
		ParseTreeWalker.DEFAULT.walk((ParseTreeListener)l, parser.output());

		return new TreeCommandOutput(l.allPath, output);
	}

	// Equality only considers the parsed paths, not the raw output
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TreeCommandOutput)) return false;

		return Objects.equals(paths, ((TreeCommandOutput) o).paths);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(paths);
	}

	private static class ExtractTreeModel extends TreeCommandOutputBaseListener {
		final List<String> allPath = new ArrayList<>();
		final Deque<String> currentPath = new ArrayDeque<>();
		String lastPath = "";
		int currentLevel = 0;

		@Override
		public void enterTreeHeader(TreeHeaderContext ctx) {
			lastPath = ctx.getText();
		}

		@Override
		public void enterEntry(EntryContext ctx) {
			int level = ctx.level().Indent().size();
			if (level > currentLevel) {
				currentPath.addLast(lastPath);
			}

			while (level < currentLevel) {
				currentPath.removeLast();
				currentLevel--;
			}

			lastPath = ctx.element().getText();
			currentLevel = level;

			List<String> pathSegment = new ArrayList<>();
			pathSegment.addAll(currentPath);
			pathSegment.add(lastPath);
			String path = String.join("/", pathSegment);
			allPath.add(path);
		}
	}
}
