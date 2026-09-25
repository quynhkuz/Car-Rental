package com.example.test.model.driver

import com.google.gson.annotations.SerializedName

data class DriverDocumentRequest(
    @SerializedName("cccdNo") val cccdNo: String,
    @SerializedName("gplxNo") val gplxNo: String,
    @SerializedName("gplxClass") val gplxClass: String,
    @SerializedName("gplxExpiry") val gplxExpiry: String,
    @SerializedName("frontUrl") val frontUrl: String?,
    @SerializedName("backUrl") val backUrl: String?,
    @SerializedName("selfieUrl") val selfieUrl: String?
)
