package io.github.posaydone.kinopub.core.model

import com.google.gson.annotations.SerializedName

data class UserProfileInfo(
    @SerializedName("user_id") val userId: Int,
    val email: String,
    @SerializedName("is_pro") val isPro: Boolean,
    @SerializedName("is_pro_plus") val isProPlus: Boolean,
    val login: String,
    val avatar: String?,
    @SerializedName("pro_date") val proDate: String?,
    val ga: String?,
    val server: String?,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("register_date") val registerDate: String?,
    @SerializedName("pro_days_left") val proDaysLeft: Int?,
)