plugins {
    id("org.jetbrains.kotlin.jvm") version  "1.3.70"
    id("com.google.protobuf") version "0.8.8"
    id("com.github.johnrengelman.shadow") version "4.0.4"
    id("com.devsoap.plugin.vaadin") version "1.4.1"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
}

group = "ua.nedz.demo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

apply(plugin = "com.devsoap.plugin.vaadin")
apply(plugin = "kotlin")

val grpcVersion = "1.28.1"

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compile("com.google.api.grpc:proto-google-common-protos:1.0.0")
    compile("io.grpc:grpc-netty:${grpcVersion}")
    compile("io.grpc:grpc-protobuf:${grpcVersion}")
    compile("io.grpc:grpc-stub:${grpcVersion}")
    compile(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.3.5")
    compile(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-guava", version = "1.3.5")
    compile("com.vaadin:vaadin-push:8.1.8")
    compile("io.grpc:grpc-kotlin-stub:0.1.1")

    compile(project(":death-star-api"))

    testImplementation(group = "junit", name = "junit", version = "4.12")
    testImplementation(group = "com.nhaarman", name = "mockito-kotlin", version = "1.6.0")
    testImplementation("io.grpc:grpc-testing:${grpcVersion}")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes("Main-Class" to "ua.nedz.demo.DeathStarServerKt")
    }
}