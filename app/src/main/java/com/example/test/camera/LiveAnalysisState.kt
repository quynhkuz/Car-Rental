package com.example.test.camera

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import org.fairscan.imageprocessing.ImageSize
import org.fairscan.imageprocessing.Quad



@Immutable
data class LiveAnalysisState(
    val inferenceTime: Long = 0L,
    val maskSize: ImageSize? = null,
    val binaryMaskProvider: () -> Bitmap? = { -> null },
    val stableQuad: Quad? = null,
)

sealed class ImportState {
    object Idle : ImportState()
    object Selecting : ImportState()
    data class Importing(val processed: Int, val total: Int) : ImportState()
}

//data class CameraUiState(
//    val pageCount: Int,
//    val liveAnalysisState: LiveAnalysisState,
//    val captureState: CaptureState,
//    val importState: ImportState,
//    val showCaptureError: Boolean,
//    val isLandscape: Boolean,
//    val isDebugMode: Boolean,
//    val isTorchEnabled: Boolean,
//)
