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

package io.spring.projectapi.web.error;

import io.spring.projectapi.contentful.NoSuchContentfulProjectDocumentationFoundException;
import io.spring.projectapi.contentful.NoSuchContentfulProjectException;
import io.spring.projectapi.contentful.NoUniqueContentfulProjectException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * {@link RestControllerAdvice @RestControllerAdvice} to handle known exceptions.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
@RestControllerAdvice
public class ExceptionAdvice {

	private static final ResponseEntity<Object> NOT_FOUND = ResponseEntity.notFound().build();

	@ExceptionHandler
	private ResponseEntity<?> noSuchContentfulProjectExceptionHandler(NoSuchContentfulProjectException ex) {
		return NOT_FOUND;
	}

	@ExceptionHandler
	private ResponseEntity<?> noUniqueContentfulProjectExceptionHandler(NoUniqueContentfulProjectException ex) {
		return NOT_FOUND;
	}

	@ExceptionHandler
	private ResponseEntity<?> noSuchContentfulProjectDocumentationExceptionHandler(
			NoSuchContentfulProjectDocumentationFoundException ex) {
		return NOT_FOUND;
	}

}
