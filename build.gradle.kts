plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "pl.fvst.itemshop"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}