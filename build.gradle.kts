import com.diffplug.gradle.spotless.SpotlessExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


// See: https://github.com/slackhq/slack-lints/blob/main/build.gradle.kts
plugins {
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.lint) apply false
    alias(libs.plugins.kotlinJvm) apply false
}

val jdk = libs.versions.jdk.get().toInt()
val lintJvmTargetString: String = libs.versions.lintJvmTarget.get()
val runtimeJvmTargetString: String = libs.versions.runtimeJvmTarget.get()

allprojects {
    apply(plugin = "com.diffplug.spotless")
    configure<SpotlessExtension> {
        format("misc") {
            target("*.md", ".gitignore")
            trimTrailingWhitespace()
            endWithNewline()
        }
        kotlin {
            ktlint()
            target("**/*.kt")
            trimTrailingWhitespace()
            endWithNewline()
            licenseHeaderFile(rootProject.file("spotless/spotless.kt"))
            targetExclude("**/spotless.kt")
        }
        kotlinGradle {
            ktlint()
            trimTrailingWhitespace()
            endWithNewline()
            licenseHeaderFile(
                rootProject.file("spotless/spotless.kt"),
                "(import|plugins|buildscript|dependencies|pluginManagement)"
            )
        }
    }
}

subprojects {
    val jvmTargetString =
        if (path == ":abecedary-lint") {
            lintJvmTargetString
        } else {
            runtimeJvmTargetString
        }

    val jvmTargetInt = jvmTargetString.toInt()
    pluginManager.withPlugin("java") {
        configure<JavaPluginExtension> {
            toolchain { languageVersion.set(JavaLanguageVersion.of(jdk)) }
        }

        tasks.withType<JavaCompile>().configureEach { options.release.set(jvmTargetInt) }
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(jvmTargetString))
                // TODO re-enable if lint ever targets latest kotlin versions
                //  allWarningsAsErrors.set(true)
                //  freeCompilerArgs.add("-progressive")
            }
        }
    }

    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        apply(plugin = "org.jetbrains.dokka")

        tasks.withType<DokkaTask>().configureEach {
            outputDirectory.set(layout.buildDirectory.dir("docs/partial"))
            dokkaSourceSets.configureEach { skipDeprecated.set(true) }
        }

        configure<MavenPublishBaseExtension> {
            publishToMavenCentral(host = SonatypeHost.S01)
            signAllPublications()
        }
    }
}
