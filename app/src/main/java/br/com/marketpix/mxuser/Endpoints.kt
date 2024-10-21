package br.com.marketpix.mxuser

import br.com.marketpix.mxuser.types.PackedOffer
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url

val prodApis  = listOf("https://api1.marketpix.com.br:9510", "https://api2.marketpix.com.br:9510")

interface Endpoints {
    @POST("/makeOffer")
    @Headers("Content-Type: application/json")
    suspend fun makeOffer(@Body body: PackedOffer?) : Response<JsonObject>

    @POST
    @Headers("Content-Type: application/json")
    suspend fun makeOffer2(@Url url: String, @Body body: PackedOffer?) : Response<JsonObject>

    @GET
    suspend fun findServer(@Url url: String) : Response<String>
}