package com.example.myapplication1

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface Endpoints {
    @POST("/makeOffer")
    @Headers("Content-Type: application/json")
    suspend fun makeOffer(@Body body: PackedOffer?) : Response<JsonObject>
}