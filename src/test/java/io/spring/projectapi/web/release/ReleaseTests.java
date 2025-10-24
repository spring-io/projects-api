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

package io.spring.projectapi.web.release;

import io.spring.projectapi.web.release.Release.Status;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link Release}.
 *
 * @author Phillip Webb
 */
class ReleaseTests {

	@Nested
	class StatusTests {

		@Test
		void fromVersionWhenVersionIsNullThrowsException() {
			assertThatIllegalArgumentException().isThrownBy(() -> Status.fromVersion(null))
				.withMessage("'version' must not be null");
		}

		@Test
		void fromVersionWhenEndsSnapshotReturnsSnapshot() {
			assertThat(Status.fromVersion("1.2.3-SNAPSHOT")).isEqualTo(Status.SNAPSHOT);
		}

		@Test
		void fromVersionWhenMilestonePatternReturnsPreRelease() {
			assertThat(Status.fromVersion("1.2.3-M4")).isEqualTo(Status.PRERELEASE);
		}

		@Test
		void fromVersionWhenRcPatternReturnsPreRelease() {
			assertThat(Status.fromVersion("1.2.3-RC2")).isEqualTo(Status.PRERELEASE);
		}

		@Test
		void fromVersionWhenGeneralAvailabilityReleaseReturnsGeneralAvailability() {
			assertThat(Status.fromVersion("1.2.3")).isEqualTo(Status.GENERAL_AVAILABILITY);
		}

	}

}
