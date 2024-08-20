package com.example.sneakerhub.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetworkHelper {
    companion object {
        //justpaste.it/6l88f
         fun checkForInternet(context: Context): Boolean {
            // register activity with the connectivity manager service
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            //This will work for Android M and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                // Representation of the capabilities of an active network.
                val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
                return when {
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    // Indicates this network uses a Cellular transport. or
                    // Cellular has network connectivity
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    // else return false
                    else -> false
                }
            } else {
                // if the android version is below M
                @Suppress("DEPRECATION") val networkInfo =
                    connectivityManager.activeNetworkInfo ?: return false
                @Suppress("DEPRECATION")
                return networkInfo.isConnected
            }//end else
        }//end check net function
    }//end companion
}//end class