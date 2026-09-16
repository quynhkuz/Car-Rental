plugins {
    alias(libs.plugins.android.application)
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

    implementation("com.karumi:dexter:6.2.3")

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.opencv)


    implementation(libs.litert)
//    implementation(libs.litert.support)
    implementation(libs.litert.metadata)

    implementation(libs.tesseract4android)


    implementation("com.github.bumptech.glide:glide:5.0.9")


}