plugins {
    id("org.jetbrains.kotlin.jvm") version  "1.3.70"
    id("com.google.protobuf") version "0.8.8"
    id("com.github.johnrengelman.shadow") version "4.0.4"

}

allprojects {
    group = "ua.nedz.demo"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    apply(plugin = "kotlin")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
