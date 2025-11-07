plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("kapt") version "2.2.20"
}

kapt {
    arguments {
        arg("eclipselink.persistencexml", "src/main/resources/META-INF/persistence.xml")
    }
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
//    implementation("mysql:mysql-connector-java:8.0.33")

    // H2
    implementation("com.h2database:h2:2.2.224")

    // Logger
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // JPA
    implementation("org.eclipse.persistence:eclipselink:4.0.2")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    kapt("org.eclipse.persistence:org.eclipse.persistence.jpa.modelgen.processor:3.0.4")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}