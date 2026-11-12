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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AsciidocCalloutNormalizer {
	private AsciidocCalloutNormalizer() {}

	public static String normalize(String content) {
		Pattern pattern = Pattern.compile("\\s*(//|#)\\s*(<\\d+>|\\(\\d+\\))\\s*$", Pattern.MULTILINE);
		Matcher matches = pattern.matcher(content);
		return matches.replaceAll("");
	}
}
