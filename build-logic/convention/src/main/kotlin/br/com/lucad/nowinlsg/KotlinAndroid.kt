package br.com.lucad.nowinlsg

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    val compileSdkVersion = libs.findVersion("compileSdk").get().toString().toInt()
    val minSdkVersion = libs.findVersion("minSdk").get().toString().toInt()

    commonExtension.apply {
        compileSdk {
            version = release(compileSdkVersion)
        }

        defaultConfig.apply {
            minSdk = minSdkVersion
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions.apply {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }

    configureKotlin()
}

private fun Project.configureKotlin() {
    val warningsAsErrors = providers.gradleProperty("warningsAsErrors")
        .map { it.toBoolean() }
        .orElse(false)

    extensions.configure<KotlinBaseExtension> {
        if (this is HasConfigurableKotlinCompilerOptions<*>) {
            compilerOptions {
                if (this is KotlinJvmCompilerOptions) {
                    jvmTarget = JvmTarget.JVM_21
                }
                allWarningsAsErrors = warningsAsErrors
                freeCompilerArgs.addAll(
                    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                )
            }
        }
    }
}
