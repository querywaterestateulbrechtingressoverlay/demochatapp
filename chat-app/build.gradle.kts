plugins {
	java
	id("org.springframework.boot") version "4.0.0-M2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.qweuio"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	// kafka
	implementation("org.springframework.kafka:spring-kafka:4.0.0-M2")
	// redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis:4.0.0-M2")
	implementation("tools.jackson.core:jackson-databind:3.0.0-rc8")
	// ws and messaging
	implementation("org.springframework:spring-messaging:7.0.0-M8")
	implementation("org.springframework.boot:spring-boot-starter-websocket:3.5.5")
	implementation("org.springframework.security:spring-security-messaging:6.5.3")
	// security and jwt
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	// persistence
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.data:spring-data-commons:4.0.0-M5")
	implementation("org.springframework.data:spring-data-jdbc:4.0.0-M5")
	implementation("org.springframework.data:spring-data-relational:4.0.0-M5")
	implementation("org.jspecify:jspecify:1.0.0")
	runtimeOnly("org.postgresql:postgresql")
	// testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.testcontainers:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
