// Copyright (C) 2023 David Rawson
// SPDX-License-Identifier: Apache-2.0
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "abecedary"
include(":abecedary-annotation")
include(":abecedary-detekt")
include(":abecedary-lint")
include(":abecedary-sample")
