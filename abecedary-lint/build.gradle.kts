// Copyright (C) 2023 David Rawson
// SPDX-License-Identifier: Apache-2.0
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// See https://github.com/slackhq/slack-lints/blob/main/slack-lint-checks/build.gradle.kts
plugins {
    kotlin("jvm")
    // Run lint on the lints! https://groups.google.com/g/lint-dev/c/q_TVEe85dgc
    alias(libs.plugins.lint)
    alias(libs.plugins.mavenPublish)
}

lint {
    htmlReport = true
    xmlReport = true
    textReport = true
    absolutePaths = false
    checkTestSources = true
    baseline = file("lint-baseline.xml")
}

dependencies {
    compileOnly(libs.lint.api)
    compileOnly(libs.lint.checks)
    testImplementation(libs.lint.tests)
    testImplementation(libs.lint.checks)
    testImplementation(libs.junit4)
}
val lintKotlinVersion = KotlinVersion(1, 9, 23)

val kgpKotlinVersion =
    KotlinVersion.fromVersion(lintKotlinVersion.toString().substringBeforeLast('.'))

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        // Lint forces Kotlin (regardless of what version the project uses), so this
        // forces a matching language level for now. Similar to `targetCompatibility` for Java.
        apiVersion.set(kgpKotlinVersion)
        languageVersion.set(kgpKotlinVersion)
    }
}
