plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.nawaf.kasirpas"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nawaf.kasirpas"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.keystore")
            storePassword = "123456"
            keyAlias = "key0"
            keyPassword = "123456"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    tasks.whenTaskAdded {
        if (name.contains("assembleDebug")) {
            doLast {
                copy {
                    from("build/outputs/apk/debug/app-debug.apk")
                    into("build/outputs/apk/debug/")
                    rename("app-debug.apk", "LuxePOS.apk")
                }
            }
        }
        if (name.contains("assembleRelease")) {
            doLast {
                copy {
                    from("build/outputs/apk/release/app-release.apk")
                    into("build/outputs/apk/release/")
                    rename("app-release.apk", "LuxePOS.apk")
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.github.DantSu:ESCPOS-ThermalPrinter-Android:3.3.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation(libs.androidx.activity)

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    implementation("androidx.fragment:fragment-ktx:1.6.2")

    implementation("io.coil-kt:coil:2.6.0")
    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")


    // =========================
    // COMPOSE
    // =========================

    implementation(platform(libs.androidx.compose.bom))

    implementation("androidx.activity:activity-compose:1.9.2")

    implementation("androidx.compose.ui:ui")

    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")
    implementation("com.airbnb.android:lottie-compose:6.7.1")

    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
