plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.quickbook"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.quickbook"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Room dependencies
    val room_version = "2.8.4"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // WorkManager
    val work_version = "2.9.0"
    implementation("androidx.work:work-runtime:$work_version")

    // Jakarta Mail
    implementation("com.sun.mail:jakarta.mail:2.0.1")

    // Security
    implementation("androidx.security:security-crypto:1.1.0")

    // Fragments
    implementation("androidx.fragment:fragment-ktx:1.8.1")

    // Navigation Component
    val nav_version = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
}