package com.example.myapplication1

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkUtils {

    companion object {
        private fun getRetrofitInstance() : Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://144-22-213-47.marketpix.com.br:9610")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        val shopApi: Endpoints = getRetrofitInstance().create(Endpoints::class.java)
    }


}