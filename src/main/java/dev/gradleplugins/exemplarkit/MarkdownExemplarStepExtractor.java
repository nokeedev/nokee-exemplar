package dev.gradleplugins.exemplarkit;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MarkdownExemplarStepExtractor implements ExemplarStepExtractor {
	private static final EmbeddedCommandExemplarStepExtractor STEP_EXTRACTOR = new EmbeddedCommandExemplarStepExtractor();

	@Override
	public List<Step> extract(String content) {
		List<Step> result = new ArrayList<>();
		Parser parser = Parser.builder().build();
		Node document = parser.parse(content);
		document.accept(new AbstractVisitor() {
			@Override
			public void visit(FencedCodeBlock codeBlock) {
				if (codeBlock.getInfo().startsWith("shell") && codeBlock.getInfo().contains("exemplar")) {
					result.addAll(STEP_EXTRACTOR.extract(codeBlock.getLiteral()));
				}
			}
		});
		return result;
	}
}
