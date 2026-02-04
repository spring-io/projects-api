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

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.spring.projectapi.github.ProjectDocumentation;
import io.spring.projectapi.github.ProjectDocumentation.Status;
import io.spring.projectapi.web.generation.Generation.LatestPatch;
import org.apache.maven.artifact.versioning.ComparableVersion;

import org.springframework.util.CollectionUtils;

/**
 * Util to resolve the latest patch version for a generation.
 *
 * @author Madhura Bhave
 */
final class PatchVersionResolver {

	private static final Pattern VERSION_REGEX = Pattern
		.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(\\.\\d+)?(?:[.|-]([^0-9]+)(\\d+)?)?$");

	private static final Pattern RELEASE_TRAIN_REGEX = Pattern.compile("([A-Za-z]*)(_|-|.)([A-Za-z0-9_-]*)");

	private static final Pattern LEGACY_VERSION_REGEX = Pattern.compile("^(\\d+)\\.(\\d+)(?:[.|-]([^0-9]+)(\\d+)?)?$");

	private static final Comparator<ProjectDocumentation> VERSION_COMPARATOR = PatchVersionResolver::compare;

	private PatchVersionResolver() {

	}

	/**
	 * Resolve the latest patch versions for a given generation, returning both OSS and
	 * Enterprise versions when available.
	 * @param generationName the generation name (e.g., "3.2.x")
	 * @param ossDocumentations oss project documentations
	 * @param enterpriseDocumentations enterprise project documentations
	 * @return the latest patch or null if neither OSS nor Enterprise patches are found
	 */
	static LatestPatch resolveLatestPatch(String generationName, List<ProjectDocumentation> ossDocumentations,
			List<ProjectDocumentation> enterpriseDocumentations) {
		String ossVersion = findLatestVersion(generationName, ossDocumentations);
		String enterpriseVersion = findLatestVersion(generationName, enterpriseDocumentations);
		if (ossVersion == null && enterpriseVersion == null) {
			return null;
		}
		return new LatestPatch(ossVersion, enterpriseVersion);
	}

	private static String findLatestVersion(String generationName, List<ProjectDocumentation> documentations) {
		if (CollectionUtils.isEmpty(documentations)) {
			return null;
		}
		String generationPrefix = extractGenerationPrefix(generationName);
		if (generationPrefix == null) {
			return null;
		}
		return documentations.stream()
			.filter((doc) -> Status.GENERAL_AVAILABILITY.equals(doc.getStatus()))
			.filter((doc) -> matchesGeneration(doc.getVersion(), generationPrefix))
			.sorted(VERSION_COMPARATOR)
			.findFirst()
			.map(ProjectDocumentation::getVersion)
			.orElse(null);
	}

	private static String extractGenerationPrefix(String generationName) {
		if (generationName == null || !generationName.endsWith(".x")) {
			return null;
		}
		return generationName.substring(0, generationName.length() - 2);
	}

	private static boolean matchesGeneration(String version, String generationPrefix) {
		if (version == null) {
			return false;
		}
		return generationPrefix.equals(getVersionWithoutPatch(version));
	}

	private static int compare(ProjectDocumentation o1, ProjectDocumentation o2) {
		ComparableVersion version1 = new ComparableVersion(o1.getVersion());
		ComparableVersion version2 = new ComparableVersion(o2.getVersion());
		return -version1.compareTo(version2);
	}

	static String getVersionWithoutPatch(String version) {
		String standardVersion = parseStandardVersion(version);
		if (standardVersion != null) {
			return standardVersion;
		}
		String releaseTrainVersion = parseReleaseTrain(version);
		if (releaseTrainVersion != null) {
			return releaseTrainVersion;
		}
		return parseLegacyVersion(version);
	}

	private static String parseStandardVersion(String text) {
		Matcher matcher = VERSION_REGEX.matcher(text);
		if (!matcher.matches()) {
			return null;
		}
		String major = matcher.group(1);
		String minor = matcher.group(2);
		return String.format("%s.%s", major, minor);
	}

	private static String parseReleaseTrain(String text) {
		Matcher matcher = RELEASE_TRAIN_REGEX.matcher(text);
		if (!matcher.matches()) {
			return null;
		}
		return matcher.group(1);
	}

	private static String parseLegacyVersion(String text) {
		Matcher matcher = LEGACY_VERSION_REGEX.matcher(text.trim());
		if (!matcher.matches()) {
			return null;
		}
		String major = matcher.group(1);
		String minor = matcher.group(2);
		return String.format("%s.%s", major, minor);
	}

}
