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

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Interceptor.Chain;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Response.Builder;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Tests for {@link RetryInterceptor}.
 *
 * @author Madhura Bhave
 */
class RetryInterceptorTests {

	private RetryInterceptor interceptor;

	@BeforeEach
	void setup() {
		this.interceptor = new RetryInterceptor();
	}

	@Test
	void interceptWhenResponseSuccessfulShouldReturnResponse() throws Exception {
		testNoRetry(200);
	}

	@Test
	void interceptWhenLimitResetHeaderNotSetShouldReturnResponse() throws Exception {
		testNoRetry(400);
	}

	@Test
	void interceptWhenLimitResetGreaterThanZeroShouldRetry() throws Exception {
		Chain chain = mock(Chain.class);
		Request request = new Request.Builder().url("https://some-url.com").build();
		given(chain.request()).willReturn(request);
		given(chain.toString()).willReturn("");
		Call call = setupForRetry(chain, request);
		Response response = getResponse(request, 429, true);
		given(chain.proceed(request)).willReturn(response);
		this.interceptor.intercept(chain);
		verifyRetry(chain, request, call);
	}

	private static Call setupForRetry(Chain chain, Request request) {
		Call call = mock(Call.class);
		willReturn(call).given(chain).call();
		willReturn(call).given(call).clone();
		willReturn(request).given(call).request();
		return call;
	}

	private static void verifyRetry(Chain chain, Request request, Call call) throws IOException {
		verify(chain).request();
		verify(chain).proceed(request);
		verify(call).execute();
	}

	private void testNoRetry(int code) throws IOException {
		Chain chain = mock(Chain.class);
		Request request = new Request.Builder().url("https://some-url.com").build();
		given(chain.request()).willReturn(request);
		Response response = getResponse(request, code, false);
		given(chain.proceed(request)).willReturn(response);
		Response result = this.interceptor.intercept(chain);
		assertThat(result).isEqualTo(response);
		verify(chain).request();
		verify(chain).proceed(request);
		verifyNoMoreInteractions(chain);
	}

	private static Response getResponse(Request request, int code, boolean addLimitReset) {
		Builder builder = new Builder().request(request)
			.protocol(Protocol.HTTP_2)
			.message("")
			.code(429)
			.body(ResponseBody.create("{}", MediaType.get("application/json; charset=utf-8")));
		if (addLimitReset) {
			builder.addHeader("X-Contentful-RateLimit-Reset", "1");
		}
		return builder.build();
	}

}
