// Copyright (C) 1936 David Rawson
// SPDX-License-Identifier: Apache-2.0
plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "io.github.drawers.abecedary.sample"

    compileSdk = 33

    lint {
        warningsAsErrors = true
        baseline = file("lint-baseline.xml")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }
}

dependencies {
    compileOnly(project(":abecedary-annotation"))
    lintChecks(project(":abecedary-lint"))
}
