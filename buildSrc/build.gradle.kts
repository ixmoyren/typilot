plugins {
    id("java")
    alias(libs.plugins.kotlin)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.chicoryCompiler)
    implementation(libs.chicoryBuildTimeCompiler)
}

kotlin { jvmToolchain(25) }

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "25"
        targetCompatibility = "25"
    }
}
