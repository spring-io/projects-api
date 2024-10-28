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

package io.spring.projectapi.web.generation;

import java.time.LocalDate;

/**
 * Calculates end of support dates when only initial release date is available.
 *
 * @author Madhura Bhave
 */
final class SupportPolicyCalculator {

	private SupportPolicyCalculator() {
	}

	static LocalDate getOSSPolicyEnd(LocalDate initialDate, LocalDate ossEnforcedPolicy, String supportPolicy) {
		if (ossEnforcedPolicy != null) {
			return ossEnforcedPolicy;
		}
		SupportPolicy policy = SupportPolicy.valueOf(supportPolicy);
		return initialDate.plusMonths(policy.getOssPolicyMonths());
	}

	static LocalDate getEnterprisePolicyEnd(LocalDate initialDate, LocalDate enterpriseEnforcedPolicy,
			String supportPolicy) {
		if (enterpriseEnforcedPolicy != null) {
			return enterpriseEnforcedPolicy;
		}
		SupportPolicy policy = SupportPolicy.valueOf(supportPolicy);
		return initialDate.plusMonths(policy.getEnterprisePolicyMonths());
	}

	private enum SupportPolicy {

		SPRING_BOOT(12, 15),

		DOWNSTREAM(12, 12),

		UPSTREAM(12, 16);

		private final int ossPolicyMonths;

		private final int enterprisePolicyMonths;

		SupportPolicy(int ossPolicyMonths, int enterprisePolicyMonths) {
			this.ossPolicyMonths = ossPolicyMonths;
			this.enterprisePolicyMonths = enterprisePolicyMonths;
		}

		int getOssPolicyMonths() {
			return this.ossPolicyMonths;
		}

		int getEnterprisePolicyMonths() {
			return this.enterprisePolicyMonths;
		}

	}

}
