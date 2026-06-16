import com.diffplug.spotless.kotlin.KtfmtStep
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import run.endive.compiler.InterpreterFallback

plugins {
    id("java")
    id("idea")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.changelog)
    alias(libs.plugins.spotless)
}

group = providers.gradleProperty("pluginGroup").get()

version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()

    // IntelliJ Platform Gradle Plugin Repositories Extension - read more:
    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform { defaultRepositories() }
}

dependencies {
    implementation(libs.endiveRuntime)
    implementation(libs.endiveWasi)
    compileOnly(libs.jna)
    testImplementation(libs.junit4)

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more:
    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
        bundledModules(providers.gradleProperty("platformBundledModules").map { it.split(',') })
        testFramework(TestFrameworkType.Platform)
    }
}

spotless {
    java {
        palantirJavaFormat("2.93.0").formatJavadoc(true)
        target("src/*/java/**/*.java", "build/generated/sources/**/*.java")
    }
    kotlin {
        ktfmt("0.63").kotlinlangStyle().configure {
            it.setMaxWidth(180)
            it.setBlockIndent(4)
            it.setContinuationIndent(4)
            it.setRemoveUnusedImports(false)
            it.setTrailingCommaManagementStrategy(KtfmtStep.TrailingCommaManagementStrategy.NONE)
        }
        target("src/*/kotlin/**/*.kt", "src/*/java/**/*.kt")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktfmt("0.63").kotlinlangStyle().configure {
            it.setMaxWidth(180)
            it.setBlockIndent(4)
            it.setContinuationIndent(4)
            it.setRemoveUnusedImports(true)
        }
    }
}

kotlin { jvmToolchain(25) }

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

val generatedResources = layout.buildDirectory.dir("generated/resources/endive-compiler").get().asFile
val generatedSources = layout.buildDirectory.dir("generated/sources/endive-compiler").get().asFile

sourceSets {
    main {
        java.srcDir(generatedSources)
        resources.srcDir(generatedResources)
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "25"
        targetCompatibility = "25"
    }

    register<EndiveCompilerTask>("endiveCompile") {
        description = "Generate typalize code by chicory compile"
        wasmFile.set(file("src/main/resources/wasm/typalize_wasm-opt.wasm"))
        moduleName.set("com.github.ixmoyren.typalize.TypalizeModule")
        moduleInterface.set("com.github.ixmoyren.typalize.Core")
        targetClassFolder.set(generatedResources)
        targetSourceFolder.set(generatedSources)
        targetWasmFolder.set(generatedResources)
        interpreterFallback.set(InterpreterFallback.WARN)
        interpretedFunctions.set(setOf())
    }

    compileJava {
        dependsOn(named("endiveCompile"))
        classpath += files(generatedResources)
    }

    compileKotlin { dependsOn(named("endiveCompile")) }

    processResources { dependsOn(named("endiveCompile")) }

    test { useJUnit() }
}
