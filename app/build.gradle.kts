plugins {
    alias(libs.plugins.android.application)

    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.test"
    compileSdk = 37

    sourceSets["main"].assets.srcDir(layout.buildDirectory.dir("generated/tflite-model"))

    defaultConfig {
        applicationId = "com.example.test"
        minSdk = 26
        targetSdk = 37
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

    buildFeatures {
        viewBinding = true
//        compose = true
    }
}

apply(from = rootProject.file("new_download-tflite.gradle.kts"))

tasks.named("preBuild") {
    dependsOn("copyTFLiteModel")
}

dependencies {

    implementation(project(":imageprocessing")) {
        exclude(group = "org.openpnp", module = "opencv")
    }

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.runtime)
    implementation(libs.litert.support.api)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.opencv)


    implementation(libs.litert)
//    implementation(libs.litert.support)
    implementation(libs.litert.metadata)

    implementation(libs.tesseract4android)



    //dexter permission
    implementation("com.karumi:dexter:6.2.3")
    // auto size
    implementation("com.intuit.sdp:sdp-android:1.1.1")

    // glide
    implementation("com.github.bumptech.glide:glide:5.0.5")

    // Gson
    implementation("com.google.code.gson:gson:2.11.0")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)


}