package com.example.myapplication1

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkUtils {

    ///makeOffer
    //val signalClient = getRetrofitInstance("https://144_022_213_047.marketpix.com.br:9510")


    companion object {

        /** Retorna uma Instância do Client Retrofit para Requisições
         * @param path Caminho Principal da API
         */
        private fun getRetrofitInstance() : Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://144-22-213-47.marketpix.com.br:9610")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        val shopApi = getRetrofitInstance().create(Endpoints::class.java)
    }


}