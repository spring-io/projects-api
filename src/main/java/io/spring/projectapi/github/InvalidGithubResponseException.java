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

package io.spring.projectapi.github;

import org.springframework.http.ResponseEntity;

/**
 * {@link GithubException} thrown when an invalid response is received.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
public class InvalidGithubResponseException extends GithubException {

	InvalidGithubResponseException(Throwable cause) {
		super(cause);
	}

	InvalidGithubResponseException(String message) {
		super(message);
	}

	static void throwIfInvalid(ResponseEntity<?> response) {
		if (response == null || !response.hasBody()) {
			throw new InvalidGithubResponseException("Empty or invalid github response");
		}
	}

}
