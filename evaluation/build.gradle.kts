plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    implementation(project(":imageprocessing"))
    implementation(libs.opencvjava)
}

apply(from = rootProject.file("new_download-tflite.gradle.kts"))

sourceSets {
    main {
        resources.srcDir(layout.buildDirectory.dir("generated/tflite-model"))
    }
}

tasks.named("processResources") {
    dependsOn("copyTFLiteModel")
}