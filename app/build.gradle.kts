import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val cyberQuizIconBase64 = layout.projectDirectory.file("cyberquiz_app_icon.b64")
val generatedCyberQuizIconRes = layout.buildDirectory.dir("generated/cyberquizIcon/res")
val generateCyberQuizIcon by tasks.registering {
    inputs.file(cyberQuizIconBase64)
    outputs.dir(generatedCyberQuizIconRes)

    doLast {
        val drawableDir = generatedCyberQuizIconRes.get().dir("drawable-nodpi").asFile
        drawableDir.mkdirs()
        val encoded = cyberQuizIconBase64.asFile.readText().filterNot { it.isWhitespace() }
        drawableDir.resolve("cyberquiz_app_icon.jpg")
            .writeBytes(Base64.getDecoder().decode(encoded))
    }
}

android {
    namespace = "com.example.cyberquiz"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.cyberquiz"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BACKEND_URL", "\"http://10.0.2.2:8000\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets.getByName("main").res.srcDir(generatedCyberQuizIconRes)
}

tasks.configureEach {
    if (name.startsWith("merge") && name.endsWith("Resources")) {
        dependsOn(generateCyberQuizIcon)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
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
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}