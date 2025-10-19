plugins {
    jacoco
    `java-library`
    id("org.sonarqube") version "7.0.0.6105"
}

repositories {
    mavenCentral()
}

val flumeVersion = "1.11.0"

dependencies {
    api("org.apache.flume:flume-ng-configuration:${flumeVersion}")
    api("org.apache.flume:flume-ng-core:${flumeVersion}")
    api("org.apache.flume:flume-ng-sdk:${flumeVersion}")

    implementation("org.apache.commons:commons-lang3:3.19.0")
    implementation("org.mongodb:mongodb-driver:3.12.14")
    implementation("org.slf4j:slf4j-api:2.0.17")

    testImplementation("org.hamcrest:hamcrest:3.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.testcontainers:junit-jupiter:1.21.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

group = "art.iculate.flume"
version = "0.2"
description = "flume-mongodb-sink"

java {
     sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.setDeprecation(true)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
}

sonar {
    properties {
        property("sonar.projectKey", "nossralf_flume-mongodb-sink")
        property("sonar.organization", "nossralf")
        property("sonar.host.url", "https://sonarcloud.io")
  }
}

tasks.sonar {
    dependsOn(tasks.jacocoTestReport)
}
