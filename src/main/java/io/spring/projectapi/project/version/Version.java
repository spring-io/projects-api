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

import java.util.Objects;

import org.springframework.lang.Nullable;

/**
 * A version representation that provides a major and minor identifier. For instance,
 * {@code 1.2.5} would have a {@code major} of "1" and {@code minor} of "1.2".
 *
 * @author Stephane Nicoll
 */
public class Version {

	private final String id;

	@Nullable
	private final String major;

	@Nullable
	private final String minor;

	@Nullable
	private final Parts parts;

	Version(String id, @Nullable String major, @Nullable String minor, @Nullable Parts parts) {
		this.id = id;
		this.major = major;
		this.minor = minor;
		this.parts = parts;
	}

	public static Version from(String version) {
		return VersionParser.safeParse(version);
	}

	/**
	 * Return the version.
	 * @return the version
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Return the major qualifier.
	 * @return the major
	 */
	@Nullable
	public String getMajor() {
		return this.major;
	}

	/**
	 * Return the minor qualifier.
	 * @return the minor
	 */
	@Nullable
	public String getMinor() {
		return this.minor;
	}

	/**
	 * Return the elements of the version, if any. Does not apply for non-numeric version
	 * such as a release train.
	 * @return the parts
	 */
	@Nullable
	public Parts getParts() {
		return this.parts;
	}

	public String toGeneration() {
		return (this.minor != null) ? "%s.x".formatted(this.minor) : "n/a";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Version version)) {
			return false;
		}
		return Objects.equals(this.id, version.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.id);
	}

	@Override
	public String toString() {
		return this.id;
	}

	public record Parts(@Nullable Integer major, @Nullable Integer minor, @Nullable Integer patch,
			@Nullable Integer hotPatch) {

		/**
		 * Return a unique number for this instance that allows to compare two versions.
		 * @return a comparable number
		 */
		public long toNumber() {
			String paddedValue = paddedNumber(this.major) + paddedNumber(this.minor) + paddedNumber(this.patch)
					+ paddedNumber(this.hotPatch);
			return Long.parseLong(paddedValue);
		}

		private String paddedNumber(@Nullable Integer number) {
			if (number != null) {
				return String.format("%02d", number);
			}
			return "00";
		}

	}

}
