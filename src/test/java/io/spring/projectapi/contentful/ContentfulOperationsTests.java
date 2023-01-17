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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.contentful.java.cma.CMAClient;
import com.contentful.java.cma.ModuleEntries;
import com.contentful.java.cma.model.CMAArray;
import com.contentful.java.cma.model.CMAEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.projectapi.contentful.ProjectDocumentation.Status;
import io.spring.projectapi.web.repository.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link ContentfulOperations}.
 *
 * @author Madhura Bhave
 */
class ContentfulOperationsTests {

	private ContentfulOperations operations;

	private CMAClient client;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setup() {
		this.objectMapper = new ObjectMapper();
		this.client = mock(CMAClient.class);
		this.operations = new ContentfulOperations(this.objectMapper, this.client);
	}

	@Test
	void addProjectDocumentationWhenProjectDoesNotExistThrowsException() {
		setupNonExistentProject();
		assertThatExceptionOfType(NoSuchContentfulProjectException.class).isThrownBy(() -> this.operations
				.addProjectDocumentation("does-not-exist", getDocumentation("1.0", Status.GENERAL_AVAILABILITY)));
	}

	@Test
	void addProjectDocumentation() {
		setupProject(addRelease());
		ProjectDocumentation documentation = getDocumentation("2.0", Status.GENERAL_AVAILABILITY);
		this.operations.addProjectDocumentation("test-project", documentation);
		ArgumentCaptor<CMAEntry> captor = ArgumentCaptor.forClass(CMAEntry.class);
		verify(this.client.entries()).update(any());
		verify(this.client.entries()).publish(captor.capture());
		CMAEntry value = captor.getValue();
		List<Map<String, Object>> updatedEntry = value.getField("documentation", "en-US");
		assertThat(updatedEntry.size()).isEqualTo(2);
		assertThat(updatedEntry).extracting((map) -> map.get("version")).containsExactly("1.0", "2.0");
		assertThat(updatedEntry).extracting((map) -> map.get("current")).containsExactly(false, true);
	}

	@Test
	void addProjectDocumentationForNonCurrent() {
		setupProject(addRelease());
		ProjectDocumentation documentation = getDocumentation("0.7", Status.GENERAL_AVAILABILITY);
		this.operations.addProjectDocumentation("test-project", documentation);
		ArgumentCaptor<CMAEntry> captor = ArgumentCaptor.forClass(CMAEntry.class);
		verify(this.client.entries()).update(any());
		verify(this.client.entries()).publish(captor.capture());
		CMAEntry value = captor.getValue();
		List<Map<String, Object>> updatedEntry = value.getField("documentation", "en-US");
		assertThat(updatedEntry.size()).isEqualTo(2);
		assertThat(updatedEntry).extracting((map) -> map.get("version")).containsExactly("1.0", "0.7");
		assertThat(updatedEntry).extracting((map) -> map.get("current")).containsExactly(true, false);
	}

	@Test
	void addFirstProjectDocumentationForGARelease() {
		setupProject((maps) -> {
		});
		ProjectDocumentation documentation = getDocumentation("1.0", Status.GENERAL_AVAILABILITY);
		this.operations.addProjectDocumentation("test-project", documentation);
		ArgumentCaptor<CMAEntry> captor = ArgumentCaptor.forClass(CMAEntry.class);
		verify(this.client.entries()).update(any());
		verify(this.client.entries()).publish(captor.capture());
		CMAEntry value = captor.getValue();
		List<Map<String, Object>> updatedEntry = value.getField("documentation", "en-US");
		assertThat(updatedEntry.size()).isEqualTo(1);
		assertThat(updatedEntry).extracting((map) -> map.get("version")).containsExactly("1.0");
		assertThat(updatedEntry).extracting((map) -> map.get("current")).containsExactly(true);
	}

	@Test
	void addFirstProjectDocumentationForNonGARelease() {
		setupProject((maps) -> {
		});
		ProjectDocumentation documentation = getDocumentation("1.0-M1", Status.PRERELEASE);
		this.operations.addProjectDocumentation("test-project", documentation);
		ArgumentCaptor<CMAEntry> captor = ArgumentCaptor.forClass(CMAEntry.class);
		verify(this.client.entries()).update(any());
		verify(this.client.entries()).publish(captor.capture());
		CMAEntry value = captor.getValue();
		List<Map<String, Object>> updatedEntry = value.getField("documentation", "en-US");
		assertThat(updatedEntry.size()).isEqualTo(1);
		assertThat(updatedEntry).extracting((map) -> map.get("version")).containsExactly("1.0-M1");
		assertThat(updatedEntry).extracting((map) -> map.get("current")).containsExactly(false);
	}

	@Test
	void deleteProjectDocumentationWhenProjectDoesNotExistThrowsException() {
		setupNonExistentProject();
		assertThatExceptionOfType(NoSuchContentfulProjectException.class)
				.isThrownBy(() -> this.operations.deleteDocumentation("does-not-exist", "1.0"));
	}

	@Test
	void deleteProjectDocumentationWhenProjectDocumentationDoesNotExistThrowsException() {
		setupProject(addRelease());
		assertThatExceptionOfType(NoSuchContentfulProjectDocumentationFoundException.class)
				.isThrownBy(() -> this.operations.deleteDocumentation("test-project", "2.0"));
	}

	@Test
	void deleteProjectDocumentation() {
		setupProject((maps) -> {
			maps.add(getRelease(false));
			maps.add(Map.of("version", "2.0", "api", "http://api.com", "ref", "http://ref.com", "status",
					"GENERAL_AVAILABILITY", "repository", "RELEASE", "current", true));
		});
		this.operations.deleteDocumentation("test-project", "1.0");
		ArgumentCaptor<CMAEntry> captor = ArgumentCaptor.forClass(CMAEntry.class);
		verify(this.client.entries()).update(any());
		verify(this.client.entries()).publish(captor.capture());
		CMAEntry value = captor.getValue();
		List<Map<String, Object>> updatedEntry = value.getField("documentation", "en-US");
		assertThat(updatedEntry.size()).isEqualTo(1);
		assertThat(updatedEntry).extracting((map) -> map.get("version")).containsExactly("2.0");
		assertThat(updatedEntry).extracting((map) -> map.get("current")).containsExactly(true);
	}

	@Test
	void deleteCurrentProjectDocumentation() {
		setupProject((maps) -> {
			maps.add(getRelease(false));
			maps.add(Map.of("version", "2.0", "api", "http://api.com", "ref", "http://ref.com", "status",
					"GENERAL_AVAILABILITY", "repository", "RELEASE", "current", true));
		});
		this.operations.deleteDocumentation("test-project", "2.0");
		ArgumentCaptor<CMAEntry> captor = ArgumentCaptor.forClass(CMAEntry.class);
		verify(this.client.entries()).update(any());
		verify(this.client.entries()).publish(captor.capture());
		CMAEntry value = captor.getValue();
		List<Map<String, Object>> updatedEntry = value.getField("documentation", "en-US");
		assertThat(updatedEntry.size()).isEqualTo(1);
		assertThat(updatedEntry).extracting((map) -> map.get("version")).containsExactly("1.0");
		assertThat(updatedEntry).extracting((map) -> map.get("current")).containsExactly(true);
	}

	@Test
	void deletePreReleaseProjectDocumentation() {
		setupProject((maps) -> {
			maps.add(getRelease(true));
			maps.add(Map.of("version", "2.0-M1", "api", "http://api.com", "ref", "http://ref.com", "status",
					"PRERELEASE", "repository", "RELEASE", "current", false));
		});
		this.operations.deleteDocumentation("test-project", "2.0-M1");
		ArgumentCaptor<CMAEntry> captor = ArgumentCaptor.forClass(CMAEntry.class);
		verify(this.client.entries()).update(any());
		verify(this.client.entries()).publish(captor.capture());
		CMAEntry value = captor.getValue();
		List<Map<String, Object>> updatedEntry = value.getField("documentation", "en-US");
		assertThat(updatedEntry.size()).isEqualTo(1);
		assertThat(updatedEntry).extracting((map) -> map.get("version")).containsExactly("1.0");
		assertThat(updatedEntry).extracting((map) -> map.get("current")).containsExactly(true);
	}

	@SuppressWarnings("unchecked")
	private void setupProject(Consumer<List<Map<String, Object>>> consumer) {
		ModuleEntries entries = mock(ModuleEntries.class);
		CMAArray<CMAEntry> cmaArray = mock(CMAArray.class);
		given(this.client.entries()).willReturn(entries);
		given(entries.fetchAll(anyMap())).willReturn(cmaArray);
		List<CMAEntry> projects = new ArrayList<>();
		CMAEntry projectEntry = getProjectEntry(consumer);
		projects.add(projectEntry);
		given(cmaArray.getItems()).willReturn(projects);
		given(this.client.entries().update(projectEntry)).willReturn(projectEntry);
	}

	private CMAEntry getProjectEntry(Consumer<List<Map<String, Object>>> consumer) {
		CMAEntry projectEntry = new CMAEntry();
		List<Map<String, Object>> documentation = new ArrayList<>();
		consumer.accept(documentation);
		projectEntry.setField("documentation", "en-US", documentation);
		return projectEntry;
	}

	private Consumer<List<Map<String, Object>>> addRelease() {
		return (maps) -> maps.add(getRelease(true));
	}

	private static Map<String, Object> getRelease(boolean current) {
		return Map.of("version", "1.0", "api", "http://api.com", "ref", "http://ref.com", "status",
				"GENERAL_AVAILABILITY", "repository", "RELEASE", "current", current);
	}

	@SuppressWarnings("unchecked")
	private void setupNonExistentProject() {
		ModuleEntries entries = mock(ModuleEntries.class);
		CMAArray<CMAEntry> cmaArray = mock(CMAArray.class);
		given(this.client.entries()).willReturn(entries);
		given(entries.fetchAll(anyMap())).willReturn(cmaArray);
		given(cmaArray.getItems()).willReturn(Collections.emptyList());
	}

	private ProjectDocumentation getDocumentation(String version, Status status) {
		return new ProjectDocumentation(version, "http://api.com", "http://ref.com", status,
				Repository.RELEASE.getName(), false);
	}

}
