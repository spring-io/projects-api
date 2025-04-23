/*
 * Copyright 2012-2023 the original author or authors.
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

package io.spring.projectapi.project.version;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.spring.projectapi.project.version.Version.Parts;

import org.springframework.lang.Nullable;

/**
 * Parse version text to a {@link Version}.
 *
 * @author Stephane Nicoll
 */
abstract class VersionParser {

	private static final Pattern VERSION_REGEX = Pattern
		.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(\\.\\d+)?(?:[.|-]([^0-9]+)(\\d+)?)?$");

	private static final Pattern RELEASE_TRAIN_REGEX = Pattern.compile("([A-Za-z]*)(_|-|.)([A-Za-z0-9_-]*)");

	private static final Pattern NON_STANDARD_VERSION_REGEX = Pattern
		.compile("^(\\d+)\\.(\\d+)(?:[.|-]([^0-9]+)(\\d+)?)?$");

	/**
	 * Safely parse the specified {@code text} into a version.
	 * @param text a version text
	 * @return a {@link Version} (never {@code null})
	 */
	static Version safeParse(String text) {
		String versionText = cleanVersion(text);
		Version standardVersion = parseStandardVersion(versionText);
		if (standardVersion != null) {
			return standardVersion;
		}
		Version releaseTrainVersion = parseReleaseTrain(versionText);
		if (releaseTrainVersion != null) {
			return releaseTrainVersion;
		}
		Version nonStandardVersion = parseNonStandardVersion(versionText);
		if (nonStandardVersion != null) {
			return nonStandardVersion;
		}
		return new Version(versionText, null, null, null);
	}

	@Nullable
	private static Version parseStandardVersion(String text) {
		Matcher matcher = VERSION_REGEX.matcher(text);
		if (!matcher.matches()) {
			return null;
		}
		String major = matcher.group(1);
		String minor = matcher.group(2);
		if (major != null) {
			// This can be calVer, semVer or our legacy format
			if (minor != null && major.length() == 4 && Integer.parseInt(major) > 1970) {
				String releaseTrainName = String.format("%s.%s", major, minor);
				return new Version(text, null, releaseTrainName,
						new Parts(safeInteger(major), safeInteger(minor), safeInteger(matcher.group(3)), null));
			}
			String patch = matcher.group(3);
			String hotPatch = matcher.group(4);
			if (hotPatch != null) {
				hotPatch = hotPatch.substring(1); // Remove .
			}
			return createVersion(text, major, minor,
					new Parts(safeInteger(major), safeInteger(minor), safeInteger(patch), safeInteger(hotPatch)));
		}
		return null;
	}

	@Nullable
	private static Version parseReleaseTrain(String text) {
		Matcher matcher = RELEASE_TRAIN_REGEX.matcher(text);
		if (!matcher.matches()) {
			return null;
		}
		String name = matcher.group(1);
		return new Version(text, null, name, null);
	}

	@Nullable
	private static Version parseNonStandardVersion(String text) {
		Matcher matcher = NON_STANDARD_VERSION_REGEX.matcher(text.trim());
		if (!matcher.matches()) {
			return null;
		}
		String major = matcher.group(1);
		String minor = matcher.group(2);
		return createVersion(text, major, minor, new Parts(safeInteger(major), safeInteger(minor), null, null));
	}

	private static Version createVersion(String text, @Nullable String major, @Nullable String minor,
			@Nullable Parts parts) {
		String minorText = (minor != null) ? String.format("%s.%s", major, minor) : null;
		return new Version(text, major, minorText, parts);
	}

	private static String cleanVersion(String version) {
		try {
			String cleanVersion = URLDecoder.decode(version, StandardCharsets.UTF_8).trim();
			int i = cleanVersion.lastIndexOf("?");
			return (i != -1) ? cleanVersion.substring(i + 1) : cleanVersion;
		}
		catch (Exception ex) {
			return version;
		}
	}

	@Nullable
	private static Integer safeInteger(@Nullable String text) {
		try {
			if (text != null) {
				return Integer.valueOf(text);
			}
		}
		catch (NumberFormatException ex) {
			// ignore
		}
		return null;
	}

}
