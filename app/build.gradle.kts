plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtools.ksp)
}

android {
    namespace = "com.fatkhun.etemu"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fatkhun.etemu"
        minSdk = 21
        targetSdk = 36

        renderscriptSupportModeEnabled = true
        multiDexEnabled = true
        vectorDrawables {
            useSupportLibrary = true
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        applicationVariants.all {
            val variant = this
            variant.outputs
                .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                .forEach { outputFile ->
                    if (outputFile.outputFileName.endsWith(".apk")) {
                        val fileName = "$applicationId-v$versionName-c$versionCode.apk"
                        outputFile.outputFileName = fileName
                    }
                }
        }
        ndk {
            // Skip deprecated ABIs. Only required when using NDK 16 or earlier.
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    flavorDimensions += "default"

    productFlavors {
        create("etemu") {
            applicationId = "com.fatkhun.etemu"
            versionCode = 1
            versionName = "1.0.0"
            dimension = "default"
            matchingFallbacks += listOf("etemu")

            buildConfigField("String", "USER_ID", "\"ETEMU\"")
            buildConfigField("String", "VIA", "\"ANDROID ETEMU\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.multidex)
    implementation(libs.koin)
    implementation(libs.koin.scope)
    implementation(libs.koin.viewmodel)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation (libs.androidx.core.ktx) {
        exclude("com.android.support", "support-compat")
    }
}