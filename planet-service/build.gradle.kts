val grpcVersion = "1.28.1"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.google.api.grpc:proto-google-common-protos:1.0.0")
    implementation("io.grpc:grpc-netty:${grpcVersion}")
    implementation("io.grpc:grpc-protobuf:${grpcVersion}")
    implementation("io.grpc:grpc-stub:${grpcVersion}")
    implementation("io.grpc:grpc-kotlin-stub:0.1.1")
    implementation("com.fireflysource:firefly-kotlin-ext:4.9.4")
    implementation("com.zaxxer:HikariCP:2.6.1")
    implementation("com.vladsch.kotlin-jdbc:kotlin-jdbc:0.4.4")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.3.5")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-guava", version = "1.3.5")


    compile(project(":death-star-api"))

    testCompile("io.grpc:grpc-testing:${grpcVersion}")
    testCompile("io.kotlintest:kotlintest-runner-junit5:3.1.10")
    testCompile(group = "junit", name = "junit", version = "4.12")
    testCompile(group = "com.nhaarman", name = "mockito-kotlin", version = "1.6.0")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes("Main-Class" to "ua.nedz.demo.PlanetServerKt")
    }
    from({
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    })
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}
