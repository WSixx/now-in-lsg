plugins {
    alias(libs.plugins.nowinlsg.android.application)
    alias(libs.plugins.nowinlsg.android.compose)
    alias(libs.plugins.nowinlsg.ksp)
}

android {
    namespace = "br.com.lucad.nowinlsg"

    defaultConfig {
        applicationId = "br.com.lucad.nowinlsg"
        versionCode = 1
        versionName = "1.0"
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
