plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtools.ksp)
}

android {
    namespace = "com.fatkhun.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    defaultPublishConfig = "etemuDebug"
    flavorDimensions += "default"

    productFlavors {
        create("etemu") {
            buildConfigField("String", "PACKAGE_EXT", "\"com.fatkhun.etemu\"")
            buildConfigField("String", "USER_ID", "\"ETEMU\"")
            buildConfigField("String", "VIA", "\"ANDROID ETEMU\"")
        }
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit.convert.gson)
    implementation(libs.retrofit)
    implementation(libs.koin)
    implementation(libs.koin.scope)
    implementation(libs.koin.viewmodel)
    implementation(libs.androidx.datastore.preferences)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.material)
    implementation(libs.sdp)
    implementation(libs.ssp)
    implementation(libs.glide)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    ksp(libs.glide.kapt)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}