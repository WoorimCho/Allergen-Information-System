package com.allergen_info_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Phase 3 cutover: the BFF has no database, so no Testcontainers MySQL.
// This is now a plain "does the BFF context start" smoke test.
// import org.springframework.context.annotation.Import;

// @Import(TestcontainersConfiguration.class)
@SpringBootTest
class AllergenInformationSystemTests {

	@Test
	void contextLoads() {
	}

}
