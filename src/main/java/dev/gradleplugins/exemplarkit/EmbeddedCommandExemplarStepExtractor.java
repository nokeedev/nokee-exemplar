package dev.gradleplugins.exemplarkit;

import dev.gradleplugins.exemplarkit.asciidoc.AsciidocCommandLineParser;

import java.util.*;

import static dev.gradleplugins.exemplarkit.asciidoc.AsciidocCalloutNormalizer.normalize;

public final class EmbeddedCommandExemplarStepExtractor implements ExemplarStepExtractor{
	private static final String COMMAND_PREFIX = "$ ";
	private final Map<String, Object> attributes;

	public EmbeddedCommandExemplarStepExtractor() {
		this(Collections.emptyMap());
	}

	public EmbeddedCommandExemplarStepExtractor(Map<String, Object> attributes) {
		this.attributes = new LinkedHashMap<>(attributes);
	}

	@Override
	public List<Step> extract(String content) {
		List<Step> result = new ArrayList<>();
		String[] lines = content.split("\r?\n");
		int pos = 0;

		do {
			pos = parseOneCommand(result, lines, pos);
		} while (pos < lines.length);

		return result;
	}

	private int parseOneCommand(List<Step> result, String[] lines, int pos) {
		String commandLineString = lines[pos];
		if (!commandLineString.startsWith(COMMAND_PREFIX)) {
			throw new RuntimeException("Inline sample command " + commandLineString);
		}

		AsciidocCommandLineParser commandLine = AsciidocCommandLineParser.parse(normalize(commandLineString.substring(COMMAND_PREFIX.length())));

		StringBuilder expectedOutput = new StringBuilder();
		int nextCommand = pos + 1;
		while (nextCommand < lines.length && !lines[nextCommand].startsWith(COMMAND_PREFIX)) {
			if (nextCommand > pos + 1) {
				expectedOutput.append("\n");
			}
			expectedOutput.append(lines[nextCommand]);
			nextCommand++;
		}


		result.add(
			Step.builder()
				.execute(commandLine.getExecutable(), commandLine.getArguments())
				.output(normalize(expectedOutput.toString()))
				.attributes(attributes)
				.build());
		return nextCommand;
	}
}
