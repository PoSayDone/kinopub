package io.github.posaydone.filmix.core.data

import io.github.posaydone.filmix.core.model.AuthResponse
import io.github.posaydone.filmix.core.model.DeviceAuthorizationStatus
import io.github.posaydone.filmix.core.model.kinopub.KinoPubDeviceCodeResponse
import io.github.posaydone.filmix.core.network.dataSource.AuthRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
) {

    suspend fun requestDeviceCode(): KinoPubDeviceCodeResponse {
        return remoteDataSource.requestDeviceCode()
    }

    suspend fun pollDeviceCode(code: String): DeviceAuthorizationStatus {
        return remoteDataSource.pollDeviceCode(code)
    }

    suspend fun refresh(refreshToken: String): AuthResponse {
        return remoteDataSource.refresh(refreshToken)
    }

    suspend fun logout() {
        remoteDataSource.logout()
    }
}
