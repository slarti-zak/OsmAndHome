package de.hochschulz.osmandhome.api

import com.google.gson.annotations.SerializedName

data class HaState(
    @SerializedName("entity_id")   val entityId: String,
    @SerializedName("state")       val state: String,
    @SerializedName("attributes")  val attributes: HaAttributes,
    @SerializedName("last_updated") val lastUpdated: String? = null
)

data class HaAttributes(
    @SerializedName("latitude")       val latitude: Double?,
    @SerializedName("longitude")      val longitude: Double?,
    @SerializedName("friendly_name")  val friendlyName: String?,
    @SerializedName("entity_picture") val entityPicture: String?,
    @SerializedName("battery_level")  val batteryLevel: Int?,
    @SerializedName("gps_accuracy")   val gpsAccuracy: Float?
) {
    val hasLocation: Boolean get() = latitude != null && longitude != null
}