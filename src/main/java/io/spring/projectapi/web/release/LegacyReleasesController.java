/*
 * Copyright 2022-2023 the original author or authors.
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

import java.util.List;
import java.util.stream.Collectors;

import io.spring.projectapi.github.GithubOperations;
import io.spring.projectapi.github.ProjectDocumentation;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Legacy API for Initializr clients.
 *
 * @author Brian Clozel
 */
@RestController
public class LegacyReleasesController {

	private final GithubOperations githubOperations;

	public LegacyReleasesController(GithubOperations githubOperations) {
		this.githubOperations = githubOperations;
	}

	@GetMapping(value = "/project_metadata/spring-boot", produces = MediaType.APPLICATION_JSON_VALUE)
	public SpringBootMetadata springBootMetadata() {
		List<ProjectDocumentation> documentations = this.githubOperations.getProjectDocumentations("spring-boot");
		return new SpringBootMetadata(documentations);
	}

	public static final class SpringBootMetadata {

		private final List<SpringBootRelease> projectReleases;

		SpringBootMetadata(List<ProjectDocumentation> documentations) {
			this.projectReleases = documentations.stream()
				.map(SpringBootRelease::fromDocumentation)
				.collect(Collectors.toList());
		}

		public String getId() {
			return "spring-boot";
		}

		public String getName() {
			return "Spring Boot";
		}

		public List<SpringBootRelease> getProjectReleases() {
			return this.projectReleases;
		}

	}

	public static final class SpringBootRelease {

		private final String version;

		private final String versionDisplayName;

		private final boolean current;

		private final String releaseStatus;

		private final boolean snapshot;

		private SpringBootRelease(String version, String versionDisplayName, boolean current, String releaseStatus,
				boolean snapshot) {
			this.version = version;
			this.versionDisplayName = versionDisplayName;
			this.current = current;
			this.releaseStatus = releaseStatus;
			this.snapshot = snapshot;
		}

		public String getVersion() {
			return this.version;
		}

		public String getVersionDisplayName() {
			return this.versionDisplayName;
		}

		public boolean isCurrent() {
			return this.current;
		}

		public String getReleaseStatus() {
			return this.releaseStatus;
		}

		public boolean isSnapshot() {
			return this.snapshot;
		}

		static SpringBootRelease fromDocumentation(ProjectDocumentation projectDocumentation) {
			return new SpringBootRelease(projectDocumentation.getVersion(), projectDocumentation.getVersion(),
					projectDocumentation.isCurrent(), projectDocumentation.getStatus().name(),
					ProjectDocumentation.Status.SNAPSHOT.equals(projectDocumentation.getStatus()));
		}

	}

}
