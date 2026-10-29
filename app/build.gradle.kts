import org.gradle.internal.impldep.com.amazonaws.PredefinedClientConfigurations.defaultConfig
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

fun getPropertyOrThrow(key: String): String {
    return localProperties.getProperty(key)
        ?: error("Missing '$key' in local.properties. Add it like: $key=YOUR_VALUE")
}

android{
    namespace = "com.rabbyx.newtonixairevivededition"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rabbyx.newtonixairevivededition"
        minSdk = 24
        targetSdk = 35
        versionCode = 144
        versionName = "3.3.6"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "GEMINI_API_KEY", "\"${getPropertyOrThrow("GEMINI_API_KEY")}\"")
        buildConfigField("String", "SKETCHFAB_API_KEY", "\"${getPropertyOrThrow("SKETCHFAB_API_KEY")}\"")
        buildConfigField("String", "YOUTUBE_API_KEY", "\"${getPropertyOrThrow("YOUTUBE_API_KEY")}\"")
        buildConfigField("String", "APP_AUTH_ID", "\"${getPropertyOrThrow("APP_AUTH_ID")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX & Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)

    // Networking & Utils
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.nanohttpd)


    implementation (libs.commons.lang3)

    // Firebase (using BoM)
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.firestore)
    implementation(libs.google.firebase.storage)
    implementation(libs.firebase.analytics)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")
    implementation(libs.glide)




//

    // Google Services
    implementation(libs.play.services.auth)
    implementation(libs.play.services.base)

    // Facebook SDK
    implementation(libs.facebook.android.sdk.vlatestrelease)

    // ML / AI / Media
    implementation(libs.generativeai)
    implementation(libs.coil) // Kept Coil, removed Glide to avoid redundancy
    implementation(libs.ucrop)

    // Local .jar/.aar files
    implementation(fileTree(mapOf(
        "dir" to "libs",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf(".*")
    )))

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
