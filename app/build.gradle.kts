plugins {
    alias(libs.plugins.nowinlsg.android.application)
    alias(libs.plugins.nowinlsg.android.compose)
    alias(libs.plugins.nowinlsg.ksp)
}

val appVersionName: String = providers
    .fileContents(layout.projectDirectory.file("../version.properties"))
    .asText
    .get()
    .lineSequence()
    .map { it.trim() }
    .firstOrNull { it.startsWith("VERSION_NAME=") }
    ?.substringAfter("=")
    ?.trim()
    ?: error("VERSION_NAME not found in version.properties")

val appVersionCode: Int = appVersionName
    .substringBefore("-")
    .split(".")
    .let { parts ->
        require(parts.size == 3) { "VERSION_NAME must follow SemVer (MAJOR.MINOR.PATCH): $appVersionName" }
        (parts[0].toInt() * 10_000) + (parts[1].toInt() * 100) + parts[2].toInt()
    }

android {
    namespace = "br.com.lucad.nowinlsg"

    defaultConfig {
        applicationId = "br.com.lucad.nowinlsg"
        versionCode = appVersionCode
        versionName = appVersionName
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.bundles.compose.adaptive)
    implementation(libs.bundles.compose.ui)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.networking)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    ksp(libs.moshi.kotlin.codegen)

    testImplementation(libs.bundles.test.unit)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.bundles.test.android)
}
