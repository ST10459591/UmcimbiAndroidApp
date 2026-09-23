plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.umcimbisa"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.umcimbisa"

        minSdk = 24
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    // Firebase
    implementation(
        platform(
            "com.google.firebase:firebase-bom:34.19.0"
        )
    )

    implementation(
        "com.google.firebase:firebase-auth"
    )

    implementation(
        "com.google.firebase:firebase-firestore"
    )

    implementation(
        "com.google.firebase:firebase-messaging"
    )

    implementation(
        "com.google.firebase:firebase-analytics"
    )

    // AndroidX
    implementation(
        libs.androidx.core.ktx
    )

    implementation(
        libs.androidx.appcompat
    )

    implementation(
        libs.material
    )

    implementation(
        libs.androidx.activity
    )

    implementation(
        libs.androidx.constraintlayout
    )

    // Retrofit
    implementation(
        "com.squareup.retrofit2:retrofit:3.0.0"
    )

    implementation(
        "com.squareup.retrofit2:converter-gson:3.0.0"
    )

    // Lifecycle
    implementation(
        "androidx.lifecycle:lifecycle-runtime-ktx:2.9.2"
    )

    // Testing
    testImplementation(
        "junit:junit:4.13.2"
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )
}