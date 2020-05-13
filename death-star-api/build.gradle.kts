import com.google.protobuf.gradle.*

apply(plugin = "com.google.protobuf")
apply(plugin = "idea")

repositories {
    mavenCentral()
}

val grpcVersion = "1.28.1"
val protobufVersion = "3.5.1-1"

dependencies {
    implementation("com.google.api.grpc:proto-google-common-protos:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
    implementation("javax.annotation:javax.annotation-api:1.2")
    implementation("io.grpc:grpc-netty:${grpcVersion}")
    implementation("io.grpc:grpc-protobuf:${grpcVersion}")
    implementation("io.grpc:grpc-stub:${grpcVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.grpc:grpc-kotlin-stub:0.1.1")
}

protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:${protobufVersion}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:0.1.1"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc") {}
                id("grpckt") {}
            }
        }
    }
}