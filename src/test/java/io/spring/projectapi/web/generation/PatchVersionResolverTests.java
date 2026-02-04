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

import java.util.Collections;
import java.util.List;

import io.spring.projectapi.github.ProjectDocumentation;
import io.spring.projectapi.github.ProjectDocumentation.Status;
import io.spring.projectapi.web.generation.Generation.LatestPatch;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PatchVersionResolver}.
 *
 * @author Madhura Bhave
 */
class PatchVersionResolverTests {

	@Test
	void resolveLatestPatchReturnsBothOssAndEnterprise() {
		List<ProjectDocumentation> ossDocs = List.of(doc("3.2.10", Status.GENERAL_AVAILABILITY));
		List<ProjectDocumentation> enterpriseDocs = List.of(doc("3.2.11", Status.GENERAL_AVAILABILITY));
		LatestPatch patch = PatchVersionResolver.resolveLatestPatch("3.2.x", ossDocs, enterpriseDocs);
		assertThat(patch).isNotNull();
		assertThat(patch.getOss()).isEqualTo("3.2.10");
		assertThat(patch.getEnterprise()).isEqualTo("3.2.11");
	}

	@Test
	void resolveLatestPatchReturnsOssOnlyWhenNoMatchingEnterprise() {
		List<ProjectDocumentation> ossDocs = List.of(doc("3.2.10", Status.GENERAL_AVAILABILITY));
		List<ProjectDocumentation> enterpriseDocs = List.of(doc("3.3.15", Status.GENERAL_AVAILABILITY));
		LatestPatch patch = PatchVersionResolver.resolveLatestPatch("3.2.x", ossDocs, enterpriseDocs);
		assertThat(patch).isNotNull();
		assertThat(patch.getOss()).isEqualTo("3.2.10");
		assertThat(patch.getEnterprise()).isNull();
	}

	@Test
	void resolveLatestPatchReturnsNullWhenBothNotMatching() {
		List<ProjectDocumentation> ossDocs = List.of(doc("3.2.10", Status.GENERAL_AVAILABILITY));
		List<ProjectDocumentation> enterpriseDocs = List.of(doc("3.3.15", Status.GENERAL_AVAILABILITY));
		LatestPatch patch = PatchVersionResolver.resolveLatestPatch("4.0.x", ossDocs, enterpriseDocs);
		assertThat(patch).isNull();
	}

	@Test
	void resolveLatestPatchReturnsHighestGAVersion() {
		List<ProjectDocumentation> ossDocs = List.of(doc("3.2.10", Status.GENERAL_AVAILABILITY),
				doc("3.23.5", Status.GENERAL_AVAILABILITY), doc("3.2.11", Status.PRERELEASE),
				doc("3.2.12-SNAPSHOT", Status.PRERELEASE));
		LatestPatch patch = PatchVersionResolver.resolveLatestPatch("3.2.x", ossDocs, null);
		assertThat(patch).isNotNull();
		assertThat(patch.getOss()).isEqualTo("3.2.10");
		assertThat(patch.getEnterprise()).isNull();
	}

	@Test
	void resolveLatestPatchReturnsNullWhenInvalidGenerationName() {
		List<ProjectDocumentation> ossDocs = List.of(doc("3.2.10", Status.GENERAL_AVAILABILITY));
		LatestPatch patch = PatchVersionResolver.resolveLatestPatch("invalid", ossDocs, null);
		assertThat(patch).isNull();
	}

	@Test
	void resolveLatestPatchReturnsNullWhenNullGenerationName() {
		List<ProjectDocumentation> ossDocs = List.of(doc("3.2.10", Status.GENERAL_AVAILABILITY));
		LatestPatch patch = PatchVersionResolver.resolveLatestPatch(null, ossDocs, Collections.emptyList());
		assertThat(patch).isNull();
	}

	@Test
	void resolveLatestPatchHandlesHotfixes() {
		List<ProjectDocumentation> enterpriseDocs = List.of(doc("2.7.19.1", Status.GENERAL_AVAILABILITY),
				doc("2.7.19", Status.GENERAL_AVAILABILITY));
		LatestPatch patch = PatchVersionResolver.resolveLatestPatch("2.7.x", Collections.emptyList(), enterpriseDocs);
		assertThat(patch).isNotNull();
		assertThat(patch.getEnterprise()).isEqualTo("2.7.19.1");
		assertThat(patch.getOss()).isNull();
	}

	@Test
	void resolveLatestPatchHandlesLegacyVersions() {
		List<ProjectDocumentation> ossDocs = List.of(doc("2.7.19.RELEASE", Status.GENERAL_AVAILABILITY),
				doc("2.7.20.M1", Status.PRERELEASE));
		LatestPatch patch = PatchVersionResolver.resolveLatestPatch("2.7.x", ossDocs, Collections.emptyList());
		assertThat(patch).isNotNull();
		assertThat(patch.getOss()).isEqualTo("2.7.19.RELEASE");
	}

	@Test
	void resolveLatestPatchHandlesCalVer() {
		List<ProjectDocumentation> ossDocs = List.of(doc("2020.1.0-RC1", Status.PRERELEASE),
				doc("2020.1.5", Status.GENERAL_AVAILABILITY));
		LatestPatch patch = PatchVersionResolver.resolveLatestPatch("2020.1.x", ossDocs, Collections.emptyList());
		assertThat(patch).isNotNull();
		assertThat(patch.getOss()).isEqualTo("2020.1.5");
	}

	@Test
	void resolveLatestPatchHandlesLegacyReleaseTrainVersions() {
		List<ProjectDocumentation> ossDocs = List.of(doc("Gosling-SR1", Status.GENERAL_AVAILABILITY),
				doc("Gosling-M1", Status.PRERELEASE));
		LatestPatch patch = PatchVersionResolver.resolveLatestPatch("Gosling.x", ossDocs, Collections.emptyList());
		assertThat(patch).isNotNull();
		assertThat(patch.getOss()).isEqualTo("Gosling-SR1");
	}

	private ProjectDocumentation doc(String version, Status status) {
		return new ProjectDocumentation(version, false, "https://example.com/api", "https://example.com/ref", status,
				false);
	}

}
