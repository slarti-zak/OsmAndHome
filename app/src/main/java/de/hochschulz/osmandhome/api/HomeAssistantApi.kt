package de.hochschulz.osmandhome.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface HomeAssistantApi {
    @GET("api/states")
    suspend fun getAllStates(@Header("Authorization") token: String): List<HaState>

    @GET("api/states/{entity_id}")
    suspend fun getState(
        @Header("Authorization") token: String,
        @Path("entity_id") entityId: String
    ): HaState
}