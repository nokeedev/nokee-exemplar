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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.SystemUtils.IS_OS_FREE_BSD;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

final class ProcessRunner {
	private ProcessRunner() {}

	static List<String> shellPrefix() {
		if (IS_OS_WINDOWS) {
			return Arrays.asList("cmd", "/c");
		} else if (IS_OS_FREE_BSD) {
			return Arrays.asList("/bin/sh", "-c");
		}
		return Arrays.asList("/bin/bash", "-c");
	}

	static List<String> script(String executable, List<String> arguments) {
		List<String> line = new ArrayList<>();
		line.add(executable);
		line.addAll(arguments);

		List<String> result = new ArrayList<>(shellPrefix());
		result.add(String.join(" ", line));
		return result;
	}

	static Optional<File> findOnPath(String name) {
		String path = System.getenv("PATH");
		if (path == null || path.isEmpty()) {
			return Optional.empty();
		}

		List<String> extensions = Collections.singletonList("");
		if (IS_OS_WINDOWS) {
			String pathExt = System.getenv("PATHEXT");
			if (pathExt == null || pathExt.isEmpty()) {
				pathExt = ".COM;.EXE;.BAT;.CMD";
			}
			extensions = new ArrayList<>();
			extensions.add("");
			extensions.addAll(Arrays.asList(pathExt.split(";")));
		}

		for (String directory : path.split(File.pathSeparator)) {
			if (directory.isEmpty()) {
				continue;
			}
			for (String extension : extensions) {
				File candidate = new File(directory, name + extension);
				if (candidate.isFile() && candidate.canExecute()) {
					return Optional.of(candidate);
				}
			}
		}
		return Optional.empty();
	}

	static Result run(List<String> command, File workingDirectory) {
		File directory = workingDirectory != null ? workingDirectory : new File(System.getProperty("user.dir"));
		String commandLine = command.stream().map(String::valueOf).collect(Collectors.joining(" "));
		System.out.println(String.format("Starting process 'command '%s''. Working directory: %s Command: %s", command.get(0), directory.getAbsolutePath(), commandLine));

		ProcessBuilder builder = new ProcessBuilder(command)
			.directory(directory)
			.redirectInput(ProcessBuilder.Redirect.INHERIT);

		try {
			Process process = builder.start();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			Thread stdout = pump(process.getInputStream(), System.out, output);
			Thread stderr = pump(process.getErrorStream(), System.err, output);
			int exitValue = process.waitFor();
			stdout.join();
			stderr.join();

			String outputAsString;
			synchronized (output) {
				outputAsString = new String(output.toByteArray(), Charset.defaultCharset());
			}
			return new Result(commandLine, exitValue, outputAsString);
		} catch (IOException e) {
			throw new UncheckedIOException(String.format("Could not start process '%s'.", commandLine), e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(String.format("Interrupted while waiting for process '%s'.", commandLine), e);
		}
	}

	private static Thread pump(InputStream source, PrintStream forward, OutputStream capture) {
		Thread thread = new Thread(() -> {
			byte[] buffer = new byte[8192];
			try {
				int count;
				while ((count = source.read(buffer)) != -1) {
					synchronized (capture) {
						capture.write(buffer, 0, count);
					}
					forward.write(buffer, 0, count);
					forward.flush();
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, "process-output-pump");
		thread.setDaemon(true);
		thread.start();
		return thread;
	}

	static final class Result {
		private final String commandLine;
		private final int exitValue;
		private final String output;

		private Result(String commandLine, int exitValue, String output) {
			this.commandLine = commandLine;
			this.exitValue = exitValue;
			this.output = output;
		}

		int getExitValue() {
			return exitValue;
		}

		String getOutput() {
			return output;
		}

		String getOutputWithNormalizedEndOfLine() {
			return output.replace("\r\n", "\n");
		}

		Result assertNormalExitValue() {
			if (exitValue != 0) {
				throw new IllegalStateException(String.format("Process '%s' finished with non-zero exit value %d.%nOutput:%n%s", commandLine, exitValue, output));
			}
			return this;
		}
	}
}
