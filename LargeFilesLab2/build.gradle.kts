import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.5.30"
}

group = "me.romchirik"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("info.picocli:picocli:4.6.1")
    implementation ("io.github.microutils:kotlin-logging:2.0.11")
    implementation("org.slf4j:slf4j-simple:1.7.32")

    implementation("org.hibernate.validator:hibernate-validator-annotation-processor:7.0.1.Final")
    implementation("org.hibernate.validator:hibernate-validator:7.0.1.Final")
    implementation("org.glassfish:jakarta.el:4.0.1")
    implementation("javax.validation:validation-api:2.0.1.Final")
}

application {
    mainClass.set("nsu.titov.MainKt")
}


tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}