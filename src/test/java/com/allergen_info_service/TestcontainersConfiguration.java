package com.allergen_info_service;

// === Phase 3 cutover (2026-09-08): RETIRED - kept commented, not deleted ===
// The BFF has no database. No Testcontainers MySQL needed.
// To restore: strip the leading '// ' from each line below, remove this banner.
//
// 
// import org.springframework.boot.test.context.TestConfiguration;
// import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
// import org.springframework.context.annotation.Bean;
// import org.testcontainers.containers.MySQLContainer;
// import org.testcontainers.utility.DockerImageName;
// 
// @TestConfiguration(proxyBeanMethods = false)
// class TestcontainersConfiguration {
// 
// 	// Defines a Bean in a configuration class, gives full control over bean creation and lifecycle.
// 	// Pinned to 8.4 (the LTS the rest of the system runs — see Projects/compose.yaml).
// 	// `mysql:latest` is now 9.x, which removed the `innodb_log_file_size` server
// 	// variable that this Testcontainers version still injects -> container won't boot.
// 	@Bean
// 	@ServiceConnection
// 	MySQLContainer<?> mysqlContainer() {
// 		return new MySQLContainer<>(DockerImageName.parse("mysql:8.4"));
// 	}
// 
// }
