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

import dev.gradleplugins.exemplarkit.output.UnzipCommandOutputParser.CreateActionContext;
import dev.gradleplugins.exemplarkit.output.UnzipCommandOutputParser.ExtractActionContext;
import dev.gradleplugins.exemplarkit.output.UnzipCommandOutputParser.InflateActionContext;
import dev.gradleplugins.exemplarkit.output.UnzipCommandOutputParser.UnzipHeaderContext;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class UnzipCommandOutput {
	private final String archivePath;
	private final Set<ArchiveAction> actions;
	private final String output;

	public UnzipCommandOutput(String archivePath, Set<ArchiveAction> actions, String output) {
		this.archivePath = archivePath;
		this.actions = actions;
		this.output = output;
	}

	public String get() {
		return output;
	}

	public static UnzipCommandOutput from(String output) {
		UnzipCommandOutputLexer lexer = new UnzipCommandOutputLexer(CharStreams.fromString(output));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		UnzipCommandOutputParser parser = new UnzipCommandOutputParser(tokens);

		ExtractUnzipModel l = new ExtractUnzipModel();
		ParseTreeWalker.DEFAULT.walk((ParseTreeListener)l, parser.output());

		return new UnzipCommandOutput(l.getArchivePath(), l.getActions(), output);
	}

	// Equality only considers the parsed archive path and actions, not the raw output
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof UnzipCommandOutput)) return false;

		UnzipCommandOutput that = (UnzipCommandOutput) o;
		return Objects.equals(archivePath, that.archivePath) && Objects.equals(actions, that.actions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(archivePath, actions);
	}

	private enum ArchiveOperation {
		INFLATING, CREATING
	}

	private static final class ArchiveAction {
		private final ArchiveOperation operation;
		private final String path;

		ArchiveAction(ArchiveOperation operation, String path) {
			this.operation = Objects.requireNonNull(operation, "operation is marked non-null but is null");
			this.path = Objects.requireNonNull(path, "path is marked non-null but is null");
		}

		public ArchiveOperation getOperation() {
			return operation;
		}

		public String getPath() {
			return path;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ArchiveAction)) return false;

			ArchiveAction that = (ArchiveAction) o;
			return operation == that.operation && path.equals(that.path);
		}

		@Override
		public int hashCode() {
			return Objects.hash(operation, path);
		}

		@Override
		public String toString() {
			return "UnzipCommandOutput.ArchiveAction(operation=" + operation + ", path=" + path + ")";
		}
	}

	private static class ExtractUnzipModel extends UnzipCommandOutputBaseListener {
		private final Set<ArchiveAction> actions = new LinkedHashSet<>();
		private String archivePath;

		public Set<ArchiveAction> getActions() {
			return actions;
		}

		public String getArchivePath() {
			return archivePath;
		}

		@Override
		public void exitUnzipHeader(UnzipHeaderContext ctx) {
			archivePath = ctx.Path().getText();
		}

		@Override
		public void exitCreateAction(CreateActionContext ctx) {
			actions.add(new ArchiveAction(ArchiveOperation.CREATING, ctx.Path().getText()));
		}

		@Override
		public void exitInflateAction(InflateActionContext ctx) {
			actions.add(new ArchiveAction(ArchiveOperation.INFLATING, ctx.Path().getText()));
		}

		@Override
		public void exitExtractAction(ExtractActionContext ctx) {
			actions.add(new ArchiveAction(ArchiveOperation.INFLATING, ctx.Path().getText()));
		}
	}
}
