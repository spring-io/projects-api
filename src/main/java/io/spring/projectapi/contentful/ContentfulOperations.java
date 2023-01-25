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

package io.spring.projectapi.contentful;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.contentful.java.cma.CMAClient;
import com.contentful.java.cma.model.CMAArray;
import com.contentful.java.cma.model.CMAEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;

/**
 * Contentful operations performed via the {@link CMAClient REST API}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
class ContentfulOperations {

	private static final String LOCALE = "en-US";

	private static Comparator<Map<String, Object>> VERSION_COMPARATOR = (o1, o2) -> {
		String version1 = (String) o1.get("version");
		String version2 = (String) o2.get("version");
		return -version1.compareTo(version2);
	};

	private final CMAClient client;

	private final ObjectMapper objectMapper;

	ContentfulOperations(ObjectMapper objectMapper, String accessToken, String spaceId, String environmentId) {
		this(objectMapper, buildClient(accessToken, spaceId, environmentId));
	}

	ContentfulOperations(ObjectMapper objectMapper, CMAClient client) {
		this.objectMapper = objectMapper;
		this.client = client;
	}

	private static CMAClient buildClient(String accessToken, String spaceId, String environmentId) {
		CMAClient.Builder builder = new CMAClient.Builder();
		builder.setAccessToken(accessToken);
		builder.setSpaceId(spaceId);
		builder.setEnvironmentId(environmentId);
		OkHttpClient coreCallFactory = builder.defaultCoreCallFactoryBuilder().addInterceptor(new RetryInterceptor())
				.build();
		builder.setCoreCallFactory(coreCallFactory);
		return builder.build();
	}

	void addProjectDocumentation(String projectSlug, ProjectDocumentation documentation) {
		CMAEntry projectEntry = getProjectEntry(projectSlug);
		List<Map<String, Object>> releases = projectEntry.getField("documentation", LOCALE);
		Map<String, Object> documentationMap = convertToMap(documentation);
		releases.add(documentationMap);
		computeCurrentRelease(releases);
		CMAEntry updated = this.client.entries().update(projectEntry);
		this.client.entries().publish(updated);
	}

	private void computeCurrentRelease(List<Map<String, Object>> releases) {
		List<Map<String, Object>> modifiableReleases = releases.stream().map(initializeToFalse())
				.collect(Collectors.toList());
		releases.clear();
		releases.addAll(modifiableReleases);
		modifiableReleases.stream().sorted(VERSION_COMPARATOR)
				.filter((documentation) -> "GENERAL_AVAILABILITY".equals(documentation.get("status"))).findFirst()
				.ifPresent((documentation) -> documentation.put("current", true));
	}

	private static Function<Map<String, Object>, LinkedHashMap<String, Object>> initializeToFalse() {
		return (map) -> {
			LinkedHashMap<String, Object> modifiableMap = new LinkedHashMap<>(map);
			modifiableMap.put("current", false);
			return modifiableMap;
		};
	}

	void deleteDocumentation(String projectSlug, String version) {
		CMAEntry projectEntry = getProjectEntry(projectSlug);
		List<Map<String, Object>> documentations = projectEntry.getField("documentation", LOCALE);
		NoSuchContentfulProjectDocumentationFoundException.throwIfHasNotPresent(documentations, projectSlug, version);
		documentations.removeIf((documentation) -> documentation.get("version").equals(version));
		computeCurrentRelease(documentations);
		CMAEntry updated = this.client.entries().update(projectEntry);
		this.client.entries().publish(updated);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> convertToMap(ProjectDocumentation documentation) {
		return this.objectMapper.convertValue(documentation, Map.class);
	}

	private CMAEntry getProjectEntry(String projectSlug) {
		Map<String, String> query = Map.of("content_type", "project", "fields.slug", projectSlug);
		CMAArray<CMAEntry> entries = this.client.entries().fetchAll(query);
		List<CMAEntry> items = entries.getItems();
		NoSuchContentfulProjectException.throwIfEmpty(items, projectSlug);
		NoUniqueContentfulProjectException.throwIfNoUniqueResult(items, projectSlug);
		return items.get(0);
	}

}
