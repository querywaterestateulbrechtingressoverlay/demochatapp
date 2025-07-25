plugins {
	java
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.qweuio"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.3")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.security:spring-security-messaging:6.4.3")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework:spring-messaging:6.2.3")
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("org.springframework.boot:spring-boot-starter-security")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("com.redis:testcontainers-redis")
	testImplementation("org.testcontainers:mongodb")
	testImplementation("org.testcontainers:kafka")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
