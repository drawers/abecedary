import kotlin.script.experimental.jvm.util.classpathFromFQN

//@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    compileOnly(project(":abecedary-annotation"))
    lintChecks(project(":abecedary-lint"))
}
