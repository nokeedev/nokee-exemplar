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

import dev.gradleplugins.exemplarkit.EmbeddedCommandExemplarStepExtractor;
import dev.gradleplugins.exemplarkit.Step;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;

import java.util.ArrayList;
import java.util.List;

final class AsciidoctorExemplarStepExtractor implements AsciidoctorContent.Visitor {
	private final List<Step> steps = new ArrayList<>();

	@Override
	public void visit(Document node) {

	}

	@Override
	public void visit(Block node) {
		if (node.isBlock() && node.hasRole("terminal")) {
			parseEmbeddedCommand(node);
		}
	}

	private void parseEmbeddedCommand(Block block) {
		steps.addAll(new EmbeddedCommandExemplarStepExtractor(block.getAttributes()).extract(block.getSource()));
	}

	public List<Step> get() {
		return steps;
	}
}
