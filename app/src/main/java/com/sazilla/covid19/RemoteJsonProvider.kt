package com.sazilla.covid19

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import java.io.IOException

enum class JSONUrl(val resId: Int) {
    National(R.string.json_url_national),
    Regional(R.string.json_url_regional),
    Provincial(R.string.json_url_provincial)
}

class RemoteJsonProvider(private val context: Context) {

    private val cache = HashMap<JSONUrl, JSONArray>()

    fun retrieveJSON(jsonUrl: JSONUrl): Maybe<JSONArray> =
        if (cache.containsKey(jsonUrl)) {
            Maybe.just(cache[jsonUrl])
        } else {
            Maybe.create<JSONArray> { emitter ->
                val url = context.getString(jsonUrl.resId)
                val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
                    Response.Listener<JSONArray> {
                        cache[jsonUrl] = it
                        emitter.onSuccess(it)
                    },
                    Response.ErrorListener {
                        emitter.onError(IOException("Failed to retrieve json from: $url"))
                    })
                Volley.newRequestQueue(context).add(jsonArrayRequest)
            }.subscribeOn(Schedulers.io())
        }
}
