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
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.contentful.java.cma.model.RateLimits;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * OkHttp {@link Interceptor} which can retry a request if the rate limit is exceeded.
 *
 * @author Madhura Bhave
 */
class RetryInterceptor implements Interceptor {

	@Override
	public Response intercept(Chain chain) throws IOException {
		Response response = chain.proceed(chain.request());
		int reset = getResetSeconds(response);
		if (response.isSuccessful() || reset == 0) {
			return response;
		}
		sleep(reset);
		response.close();
		return chain.call().clone().execute();
	}

	private static int getResetSeconds(Response response) {
		Headers headers = response.headers();
		Map<String, List<String>> mappedHeaders = headers.toMultimap();
		RateLimits limits = new RateLimits.DefaultParser().parse(mappedHeaders);
		return limits.getReset();
	}

	private void sleep(int seconds) {
		try {
			Thread.sleep(Duration.ofSeconds(seconds).toMillis());
		}
		catch (InterruptedException interruptedException) {
			Thread.currentThread().interrupt();
		}
	}

}
