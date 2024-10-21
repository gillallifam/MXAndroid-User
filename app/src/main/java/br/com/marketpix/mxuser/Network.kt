package br.com.marketpix.mxuser

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class NetworkUtils {

    companion object {
        private fun getRetrofitInstance() : Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://144-022-213-047.marketpix.com.br:9610")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        val shopApi: Endpoints = getRetrofitInstance().create(Endpoints::class.java)
    }


}