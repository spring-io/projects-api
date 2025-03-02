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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SupportPolicyCalculator}.
 *
 * @author Madhura Bhave
 */
class SupportPolicyCalculatorTests {

	@Test
	void ossSupportPolicyWhenEnforcedDateIsEmpty() {
		LocalDate ossPolicyEndSpringBoot = SupportPolicyCalculator.getOSSPolicyEnd(LocalDate.parse("2022-11-24"), null,
				"SPRING_BOOT");
		assertThat(ossPolicyEndSpringBoot).isEqualTo(LocalDate.of(2023, 11, 24));
		LocalDate ossPolicyEndUpstream = SupportPolicyCalculator.getOSSPolicyEnd(LocalDate.parse("2022-11-24"), null,
				"UPSTREAM");
		assertThat(ossPolicyEndUpstream).isEqualTo(LocalDate.of(2023, 11, 24));
		LocalDate ossPolicyEndDownstream = SupportPolicyCalculator.getOSSPolicyEnd(LocalDate.parse("2022-11-24"), null,
				"DOWNSTREAM");
		assertThat(ossPolicyEndDownstream).isEqualTo(LocalDate.of(2023, 11, 24));
	}

	@Test
	void ossSupportPolicyWhenEnforcedDateIsNotEmpty() {
		LocalDate ossPolicyEndSpringBoot = SupportPolicyCalculator.getOSSPolicyEnd(LocalDate.parse("2022-11-24"),
				LocalDate.parse("2025-02-02"), "SPRING_BOOT");
		assertThat(ossPolicyEndSpringBoot).isEqualTo(LocalDate.of(2025, 2, 2));
		LocalDate ossPolicyEndUpstream = SupportPolicyCalculator.getOSSPolicyEnd(LocalDate.parse("2022-11-24"),
				LocalDate.parse("2025-02-02"), "UPSTREAM");
		assertThat(ossPolicyEndUpstream).isEqualTo(LocalDate.of(2025, 2, 2));
		LocalDate ossPolicyEndDownstream = SupportPolicyCalculator.getOSSPolicyEnd(LocalDate.parse("2022-11-24"),
				LocalDate.parse("2025-02-02"), "DOWNSTREAM");
		assertThat(ossPolicyEndDownstream).isEqualTo(LocalDate.of(2025, 2, 2));
	}

	@Test
	void enterpriseSupportPolicyWhenEnforcedDateIsEmpty() {
		LocalDate ossPolicyEndSpringBoot = SupportPolicyCalculator.getEnterprisePolicyEnd(LocalDate.parse("2022-11-24"),
				null, "SPRING_BOOT");
		assertThat(ossPolicyEndSpringBoot).isEqualTo(LocalDate.of(2024, 2, 24));
		LocalDate ossPolicyEndUpstream = SupportPolicyCalculator.getEnterprisePolicyEnd(LocalDate.parse("2022-11-24"),
				null, "UPSTREAM");
		assertThat(ossPolicyEndUpstream).isEqualTo(LocalDate.of(2024, 3, 24));
		LocalDate ossPolicyEndDownstream = SupportPolicyCalculator.getEnterprisePolicyEnd(LocalDate.parse("2022-11-24"),
				null, "DOWNSTREAM");
		assertThat(ossPolicyEndDownstream).isEqualTo(LocalDate.of(2023, 11, 24));
	}

	@Test
	void enterpriseSupportPolicyWhenEnforcedDateIsNotEmpty() {
		LocalDate policyEndSpringBoot = SupportPolicyCalculator.getEnterprisePolicyEnd(LocalDate.parse("2022-11-24"),
				LocalDate.parse("2025-02-02"), "SPRING_BOOT");
		assertThat(policyEndSpringBoot).isEqualTo(LocalDate.of(2025, 2, 2));
		LocalDate policyEndUpstream = SupportPolicyCalculator.getEnterprisePolicyEnd(LocalDate.parse("2022-11-24"),
				LocalDate.parse("2025-02-02"), "UPSTREAM");
		assertThat(policyEndUpstream).isEqualTo(LocalDate.of(2025, 2, 2));
		LocalDate policyEndDownstream = SupportPolicyCalculator.getEnterprisePolicyEnd(LocalDate.parse("2022-11-24"),
				LocalDate.parse("2025-02-02"), "DOWNSTREAM");
		assertThat(policyEndDownstream).isEqualTo(LocalDate.of(2025, 2, 2));
	}

}
