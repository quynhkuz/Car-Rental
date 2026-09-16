import org.gradle.api.tasks.Copy
import java.net.URL

val modelVersion = "v1.2.0"
val modelFileName = "fairscan-segmentation-model.tflite"
val modelUrl = "https://github.com/pynicolas/fairscan-segmentation-model/releases/download/$modelVersion/$modelFileName"

val downloadedModelPath = layout.buildDirectory.file("downloads/$modelFileName")
val generatedModelDir = layout.buildDirectory.dir("generated/tflite-model")

val downloadTFLiteModel = tasks.register("downloadTFLiteModel") {
    val outputFile = downloadedModelPath.get().asFile
    outputs.file(outputFile)

    doLast {
        if (!outputFile.exists()) {
            println("Downloading $modelFileName from $modelUrl")
            outputFile.parentFile.mkdirs()
            URL(modelUrl).openStream().use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } else {
            println("Model already downloaded: ${outputFile.absolutePath}")
        }
    }
}

// Chỉ tải + copy model vào build/generated/tflite-model.
// Module nào apply script này thì tự gắn thư mục đó vào sourceSet của mình
// (Android: assets, java-library: resources) và vào lifecycle task phù hợp
// (Android: preBuild, java-library: processResources).
tasks.register<Copy>("copyTFLiteModel") {
    dependsOn(downloadTFLiteModel)
    from(downloadedModelPath)
    into(generatedModelDir)
}