package io.github.posaydone.filmix.core.network.service

import io.github.posaydone.filmix.core.model.AuthResponse
import io.github.posaydone.filmix.core.model.kinopub.KinoPubDeviceCodeResponse
import io.github.posaydone.filmix.core.model.kinopub.KinoPubStatusResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {
    @POST("oauth2/device")
    suspend fun requestDeviceCode(
        @Query("grant_type") grantType: String = "device_code",
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
    ): Response<KinoPubDeviceCodeResponse>

    @POST("oauth2/device")
    suspend fun pollDeviceCode(
        @Query("grant_type") grantType: String = "device_token",
        @Query("code") code: String,
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
    ): Response<AuthResponse>

    @POST("oauth2/device")
    suspend fun refresh(
        @Query("grant_type") grantType: String = "refresh_token",
        @Query("refresh_token") refreshToken: String,
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
    ): Response<AuthResponse>

    @POST("v1/device/unlink")
    suspend fun logout(): Response<KinoPubStatusResponse>
}
