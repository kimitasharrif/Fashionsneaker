package com.example.sneakerhub.helpers

import android.content.Context
import android.widget.Toast
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.entity.StringEntity
import org.json.JSONArray
import org.json.JSONObject

class ApiHelper(private val context: Context) {

    // POST
    fun post(api: String, jsonData: JSONObject, callBack: CallBack) {
        Toast.makeText(context, "Please Wait for response", Toast.LENGTH_LONG).show()
        val client = AsyncHttpClient(true, 80, 443)
        val conBody = StringEntity(jsonData.toString())
        val token = PrefsHelper.getPrefs(context, "access_token")
        client.addHeader("Authorization", "Bearer $token")
        client.addHeader("Content-Type", "application/json")

        client.post(context, api, conBody, "application/json", object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONArray?) {
                callBack.onSuccess(response)
            }

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                callBack.onSuccess(response)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                callBack.onFailure(errorResponse?.toString() ?: "Unknown error")
            }
        })
    }
    //Requires Access Token
    fun post2(api: String, jsonData: JSONObject, callBack: CallBack) {
        Toast.makeText(context, "Please Wait for response", Toast.LENGTH_LONG).show()
        val client = AsyncHttpClient(true, 80, 443)
        val con_body = StringEntity(jsonData.toString())
        val token = PrefsHelper.getPrefs(context, "access_token")
        client.addHeader("Authorization", "Bearer $token")

        //post to API
        client.post(context, api, con_body, "application/json",
            object : JsonHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    response: JSONArray?
                ) {
                    callBack.onSuccess(response)
                    //Toast.makeText(context, "Response $response ", Toast.LENGTH_SHORT).show()
                }

                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    response: JSONObject?
                ) {
                    callBack.onSuccess(response)
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    throwable: Throwable?,
                    errorResponse: JSONObject?
                ) {
                    callBack.onFailure(errorResponse.toString())

                }
            })
    }//END POST


    // GET
    fun get(api: String, callBack: CallBack) {
        val client = AsyncHttpClient(true, 80, 443)
        client.get(context, api, null, "application/json", object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONArray) {
                callBack.onSuccess(response)
            }

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                callBack.onSuccess(response)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                callBack.onFailure(responseString ?: "Unknown error")
            }
        })
    }

    // PUT
    fun put(api: String, jsonData: JSONObject) {
        Toast.makeText(context, "Please Wait for response", Toast.LENGTH_LONG).show()
        val client = AsyncHttpClient(true, 80, 443)
        val conBody = StringEntity(jsonData.toString())
        client.put(context, api, conBody, "application/json", object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                Toast.makeText(context, "Response $response", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                Toast.makeText(context, "Error Occurred: ${throwable.toString()}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // DELETE
    fun delete(api: String, jsonData: JSONObject) {
        Toast.makeText(context, "Please Wait for response", Toast.LENGTH_LONG).show()
        val client = AsyncHttpClient(true, 80, 443)
        val conBody = StringEntity(jsonData.toString())
        client.delete(context, api, conBody, "application/json", object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                Toast.makeText(context, "Response $response", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                Toast.makeText(context, "Error Occurred: ${throwable.toString()}", Toast.LENGTH_LONG).show()
            }
        })
    }

    interface CallBack {
        fun onSuccess(result: JSONArray?)
        fun onSuccess(result: JSONObject?)
        fun onFailure(result: String?)
    }
}
