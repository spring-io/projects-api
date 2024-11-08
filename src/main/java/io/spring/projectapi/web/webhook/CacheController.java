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

package io.spring.projectapi.web.webhook;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.projectapi.ApplicationProperties;
import io.spring.projectapi.ProjectRepository;
import jakarta.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that handles requests from GitHub webhook set up at
 * <a href="https://github.com/spring-io/spring-website-content">the repository level </a>
 * and triggers cache refresh. Github requests are signed with a shared secret, using an
 * HMAC sha-1 algorithm.
 *
 * @author Madhura Bhave
 */
@RestController
public class CacheController {

	private static final Logger logger = LoggerFactory.getLogger(CacheController.class);

	private static final Charset CHARSET = StandardCharsets.UTF_8;

	private static final String HMAC_ALGORITHM = "HmacSHA1";

	private static final String PING_EVENT = "ping";

	private final ObjectMapper objectMapper;

	private final Mac hmac;

	private final ProjectRepository repository;

	public CacheController(ProjectRepository repository, ApplicationProperties properties, ObjectMapper objectMapper)
			throws NoSuchAlgorithmException, InvalidKeyException {
		this.repository = repository;
		this.objectMapper = objectMapper;
		// initialize HMAC with SHA1 algorithm and secret
		SecretKeySpec secret = new SecretKeySpec(properties.getGithub().getWebhookSecret().getBytes(CHARSET),
				HMAC_ALGORITHM);
		this.hmac = Mac.getInstance(HMAC_ALGORITHM);
		this.hmac.init(secret);
	}

	private void verifyHmacSignature(String message, String signature) {
		byte[] sig = this.hmac.doFinal(message.getBytes(CHARSET));
		String computedSignature = "sha1=" + DatatypeConverter.printHexBinary(sig);
		if (!computedSignature.equalsIgnoreCase(signature)) {
			throw new WebhookAuthenticationException(computedSignature, signature);
		}
	}

	@PostMapping("/refresh_cache")
	@SuppressWarnings("unchecked")
	public ResponseEntity<String> refresh(@RequestBody String payload,
			@RequestHeader("X-Hub-Signature") String signature,
			@RequestHeader(name = "X-GitHub-Event", required = false, defaultValue = "push") String event)
			throws JsonProcessingException {
		verifyHmacSignature(payload, signature);
		if (PING_EVENT.equals(event)) {
			return ResponseEntity.ok("{ \"message\": \"Successfully processed ping event\" }");
		}
		Map<?, ?> push = this.objectMapper.readValue(payload, Map.class);
		logPayload(push);
		List<Map<String, ?>> commits = (List<Map<String, ?>>) push.get("commits");
		List<String> distinctChanges = getUpdatedFiles(commits);
		this.repository.update(distinctChanges);
		return ResponseEntity.ok("{ \"message\": \"Successfully processed cache refresh\" }");
	}

	@SuppressWarnings("unchecked")
	private static List<String> getUpdatedFiles(List<Map<String, ?>> commits) {
		List<String> changedFiles = new ArrayList<>();
		commits.forEach((commit) -> {
			List<String> added = (List<String>) commit.get("added");
			List<String> removed = (List<String>) commit.get("removed");
			List<String> modified = (List<String>) commit.get("modified");
			changedFiles.addAll(added);
			changedFiles.addAll(removed);
			changedFiles.addAll(modified);
		});
		return changedFiles.stream().distinct().toList();
	}

	@ExceptionHandler(WebhookAuthenticationException.class)
	public ResponseEntity<String> handleWebhookAuthenticationFailure(WebhookAuthenticationException exception) {
		logger.error("Webhook authentication failure: " + exception.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{ \"message\": \"Forbidden\" }");
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<String> handlePayloadParsingException(IOException exception) {
		logger.error("Payload parsing exception", exception);
		return ResponseEntity.badRequest().body("{ \"message\": \"Bad Request\" }");
	}

	private void logPayload(Map<?, ?> push) {
		if (push.containsKey("head_commit")) {
			final Object headCommit = push.get("head_commit");
			if (headCommit != null) {
				final Map<?, ?> headCommitMap = (Map<?, ?>) headCommit;
				logger.info("Received new webhook payload for push with head_commit message: "
						+ headCommitMap.get("message"));
			}
		}
		else {
			logger.info("Received new webhook payload for push, but with no head_commit");
		}
	}

}
