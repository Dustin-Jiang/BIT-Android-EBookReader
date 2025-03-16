plugins {
    alias(
        libs.plugins.android.application
    )
    alias(
        libs.plugins.kotlin.android
    )
    id("com.google.devtools.ksp")
}

android {
    namespace =
        "top.tsukino.ebookreader"
    compileSdk =
        35

    defaultConfig {
        applicationId =
            "top.tsukino.ebookreader"
        minSdk =
            24
        targetSdk =
            35
        versionCode =
            1
        versionName =
            "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled =
                false
            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_11
        targetCompatibility =
            JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget =
            "11"
    }
    buildFeatures {
        viewBinding =
            true
    }
}

dependencies {
    implementation(
        libs.androidx.navigation.fragment.ktx
    )
    implementation(
        libs.androidx.navigation.ui.ktx
    )
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")

    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    ksp("androidx.room:room-compiler:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")


    implementation(
        libs.androidx.lifecycle.livedata
    )
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
    testImplementation(
        libs.junit
    )
    androidTestImplementation(
        libs.androidx.junit
    )
    androidTestImplementation(
        libs.androidx.espresso.core
    )
}