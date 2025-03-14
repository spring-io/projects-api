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
		assertThat(ossPolicyEndSpringBoot).isEqualTo(LocalDate.of(2023, 12, 31));
		LocalDate ossPolicyEndUpstream = SupportPolicyCalculator.getOSSPolicyEnd(LocalDate.parse("2022-11-24"), null,
				"UPSTREAM");
		assertThat(ossPolicyEndUpstream).isEqualTo(LocalDate.of(2023, 12, 31));
		LocalDate ossPolicyEndDownstream = SupportPolicyCalculator.getOSSPolicyEnd(LocalDate.parse("2022-12-24"), null,
				"DOWNSTREAM");
		assertThat(ossPolicyEndDownstream).isEqualTo(LocalDate.of(2023, 12, 31));
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
				null, "SPRING_BOOT", false);
		assertThat(ossPolicyEndSpringBoot).isEqualTo(LocalDate.of(2024, 12, 31));
		LocalDate ossPolicyEndUpstream = SupportPolicyCalculator.getEnterprisePolicyEnd(LocalDate.parse("2022-11-24"),
				null, "UPSTREAM", false);
		assertThat(ossPolicyEndUpstream).isEqualTo(LocalDate.of(2024, 12, 31));
		LocalDate ossPolicyEndDownstream = SupportPolicyCalculator.getEnterprisePolicyEnd(LocalDate.parse("2022-12-24"),
				null, "DOWNSTREAM", false);
		assertThat(ossPolicyEndDownstream).isEqualTo(LocalDate.of(2024, 12, 31));
	}

	@Test
	void enterpriseSupportPolicyWhenEnforcedDateIsEmptyAndLastMinor() {
		LocalDate ossPolicyEndSpringBoot = SupportPolicyCalculator.getEnterprisePolicyEnd(LocalDate.parse("2022-11-24"),
				null, "SPRING_BOOT", true);
		assertThat(ossPolicyEndSpringBoot).isEqualTo(LocalDate.of(2029, 12, 31));
		LocalDate ossPolicyEndUpstream = SupportPolicyCalculator.getEnterprisePolicyEnd(LocalDate.parse("2022-11-24"),
				null, "UPSTREAM", true);
		assertThat(ossPolicyEndUpstream).isEqualTo(LocalDate.of(2029, 12, 31));
		LocalDate ossPolicyEndDownstream = SupportPolicyCalculator.getEnterprisePolicyEnd(LocalDate.parse("2022-12-24"),
				null, "DOWNSTREAM", true);
		assertThat(ossPolicyEndDownstream).isEqualTo(LocalDate.of(2029, 12, 31));
	}

	@Test
	void enterpriseSupportPolicyWhenEnforcedDateIsNotEmpty() {
		LocalDate policyEndSpringBoot = SupportPolicyCalculator.getEnterprisePolicyEnd(LocalDate.parse("2022-11-24"),
				LocalDate.parse("2025-02-02"), "SPRING_BOOT", false);
		assertThat(policyEndSpringBoot).isEqualTo(LocalDate.of(2025, 2, 2));
		LocalDate policyEndUpstream = SupportPolicyCalculator.getEnterprisePolicyEnd(LocalDate.parse("2022-11-24"),
				LocalDate.parse("2025-02-02"), "UPSTREAM", false);
		assertThat(policyEndUpstream).isEqualTo(LocalDate.of(2025, 2, 2));
		LocalDate policyEndDownstream = SupportPolicyCalculator.getEnterprisePolicyEnd(LocalDate.parse("2022-11-24"),
				LocalDate.parse("2025-02-02"), "DOWNSTREAM", false);
		assertThat(policyEndDownstream).isEqualTo(LocalDate.of(2025, 2, 2));
	}

}
