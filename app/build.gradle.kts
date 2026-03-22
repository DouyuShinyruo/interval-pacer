plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.github.intervalpacer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.github.intervalpacer"
        minSdk = 26  // 降低到26以覆盖更多设备
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)

    // ViewModel & Lifecycle
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Room (暂时注释，MVP 不需要)
    // implementation(libs.room.runtime)
    // implementation(libs.room.ktx)
    // ksp(libs.room.compiler)

    // Work Manager
    implementation(libs.work.runtime.ktx)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Kotlin DateTime
    implementation(libs.kotlinx.datetime)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}