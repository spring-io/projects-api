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
import java.time.temporal.TemporalAdjusters;

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
		return initialDate.plusMonths(policy.getOssPolicyMonths()).with(TemporalAdjusters.lastDayOfMonth());
	}

	static LocalDate getEnterprisePolicyEnd(LocalDate initialDate, LocalDate enterpriseEnforcedPolicy,
			String supportPolicy, boolean isLastMinor) {
		if (enterpriseEnforcedPolicy != null) {
			return enterpriseEnforcedPolicy;
		}
		SupportPolicy policy = SupportPolicy.valueOf(supportPolicy);
		if (isLastMinor) {
			return initialDate.plusMonths(policy.getExtendedPolicyMonths()).with(TemporalAdjusters.lastDayOfMonth());
		}
		return initialDate.plusMonths(policy.getEnterprisePolicyMonths()).with(TemporalAdjusters.lastDayOfMonth());
	}

	private enum SupportPolicy {

		SPRING_BOOT(13, 25, 85),

		DOWNSTREAM(12, 24, 84),

		UPSTREAM(13, 25, 85);

		private final int ossPolicyMonths;

		private final int enterprisePolicyMonths;

		private final int extendedPolicyMonths;

		SupportPolicy(int ossPolicyMonths, int enterprisePolicyMonths, int extendedPolicyMonths) {
			this.ossPolicyMonths = ossPolicyMonths;
			this.enterprisePolicyMonths = enterprisePolicyMonths;
			this.extendedPolicyMonths = extendedPolicyMonths;
		}

		int getOssPolicyMonths() {
			return this.ossPolicyMonths;
		}

		int getEnterprisePolicyMonths() {
			return this.enterprisePolicyMonths;
		}

		int getExtendedPolicyMonths() {
			return this.extendedPolicyMonths;
		}

	}

}
