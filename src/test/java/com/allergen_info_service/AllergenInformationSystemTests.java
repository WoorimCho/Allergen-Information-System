package com.allergen_info_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// The BFF has no database — just a context-starts smoke test, no Testcontainers.
@SpringBootTest
class AllergenInformationSystemTests {

	@Test
	void contextLoads() {
	}

}
