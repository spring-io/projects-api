/*
 * Copyright 2022-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.projectapi.web.generation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.spring.projectapi.web.release.Release;

/**
 * Utility to relate project versions to generations.
 *
 * @author Madhura Bhave
 */
public final class GenerationVersions {

	private static final Pattern VERSION = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\..*|-.*)?$");

	private GenerationVersions() {
	}

	/**
	 * Return the generation name (for example {@code 4.1.x}) of the given version.
	 * @param version the version
	 * @return the generation name or {@code null} if the version is not recognized
	 */
	public static String generationOf(String version) {
		Matcher matcher = VERSION.matcher((version != null) ? version : "");
		return matcher.matches() ? matcher.group(1) + "." + matcher.group(2) + ".x" : null;
	}

	/**
	 * Return the reason why the given version cannot be a latest version for the given
	 * generation.
	 * @param generation the generation name
	 * @param version the version
	 * @return the reason or {@code null} if the version is valid
	 */
	public static String validateLatestVersion(String generation, String version) {
		if (!generation.equals(generationOf(version))) {
			return "Version '%s' does not belong to generation '%s'".formatted(version, generation);
		}
		if (Release.Status.SNAPSHOT.equals(Release.Status.fromVersion(version))) {
			return "Snapshot version '%s' cannot be the latest version".formatted(version);
		}
		return null;
	}

}
