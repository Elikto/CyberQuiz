plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val cyberVersionCode = providers.gradleProperty("cyberVersionCode").orElse("1").get().toInt()
val cyberVersionName = providers.gradleProperty("cyberVersionName").orElse("1.0").get()
val roomSchemaLocation = providers.gradleProperty("roomSchemaLocation").orElse("$projectDir/schemas").get()
val cyberContactApiUrl = providers.gradleProperty("cyberContactApiUrl").orElse("").get()
val updateKeystorePath = System.getenv("CYBERQUIZ_KEYSTORE_PATH")
val updateKeystorePassword = System.getenv("CYBERQUIZ_KEYSTORE_PASSWORD")
val updateKeyAlias = System.getenv("CYBERQUIZ_KEY_ALIAS")
val updateKeyPassword = System.getenv("CYBERQUIZ_KEY_PASSWORD")

fun quotedBuildConfigValue(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    // Keep the Kotlin/Android namespace stable to avoid an unnecessary source-code move.
    // The install identity registered in Android Developer Console is applicationId below.
    namespace = "com.example.cyberquiz"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.elikto.cyberquiz"
        minSdk = 26
        targetSdk = 37
        versionCode = cyberVersionCode
        versionName = cyberVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "CONTACT_API_URL", quotedBuildConfigValue(cyberContactApiUrl))
    }

    val updateSigningConfig = if (
        !updateKeystorePath.isNullOrBlank() &&
        !updateKeystorePassword.isNullOrBlank() &&
        !updateKeyAlias.isNullOrBlank() &&
        !updateKeyPassword.isNullOrBlank()
    ) {
        signingConfigs.create("update") {
            storeFile = file(updateKeystorePath!!)
            storePassword = updateKeystorePassword
            keyAlias = updateKeyAlias
            keyPassword = updateKeyPassword
        }
    } else {
        null
    }

    buildTypes {
        debug {
            // Keep debug builds on the standard Android debug identity.
            // The distribution/update signing key is reserved for release artifacts only.
        }
        release {
            isDebuggable = false
            isJniDebuggable = false
            isMinifyEnabled = false
            if (updateSigningConfig != null) {
                signingConfig = updateSigningConfig
            }
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", roomSchemaLocation)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
