package com.scube.weatherapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log

object Constants{
    const val APP_ID : String ="1b7b55f1d45dd47a1323837b2354b405"
    const val BASE_URL :String ="http://api.openweathermap.org/data/"
    const val METRIC_UNIT :String ="metric"
    fun isNetworkAvailable(context:Context):Boolean{
        Log.i("debug"," inside the internet check")
        val connectivityManager =context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as
                ConnectivityManager
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            val network = connectivityManager.activeNetwork?:return false
            val activeNetwork =connectivityManager.getNetworkCapabilities(network)?:return false
            return when{
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)->true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)->true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)->true
                else->false
            }
        }else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnectedOrConnecting
        }

    }
}