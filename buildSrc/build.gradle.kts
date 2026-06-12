plugins {
    id("java")
    alias(libs.plugins.kotlin)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.endiveCompiler)
    implementation(libs.endiveBuildTimeCompiler)
}

kotlin { jvmToolchain(25) }

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "25"
        targetCompatibility = "25"
    }
}
