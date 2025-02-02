package dev.gradleplugins.exemplarkit;

import java.util.List;

public interface ExemplarStepExtractor {
	List<Step> extract(String content);

	static ExemplarStepExtractor forMarkdown() {
		return new MarkdownExemplarStepExtractor();
	}

	static ExemplarStepExtractor forAsciidoc() {
		throw new UnsupportedOperationException();
	}
}
