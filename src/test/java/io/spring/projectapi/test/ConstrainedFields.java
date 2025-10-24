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

package io.spring.projectapi.test;

import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.util.StringUtils;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;

/**
 * Used to document constraints on fields.
 *
 * @author Madhura Bhave
 */
public class ConstrainedFields {

	private final ConstraintDescriptions constraintDescriptions;

	public static ConstrainedFields constraintsOn(Class<?> input) {
		return new ConstrainedFields(input);
	}

	public ConstrainedFields(Class<?> input) {
		this.constraintDescriptions = new ConstraintDescriptions(input);
	}

	public FieldDescriptor withPath(String path) {
		return fieldWithPath(path).attributes(key("constraints").value(StringUtils
			.collectionToDelimitedString(this.constraintDescriptions.descriptionsForProperty(path), ". ")));
	}

}
