package com.android.launcher3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.lawnchair.LawnchairApp
import java.io.UnsupportedEncodingException
import java.net.URLDecoder


class ReferalIntentReciever : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val referrerString = intent?.getStringExtra("referrer") ?: ""

        //sending to mixpanel
        try {
            val props = LawnchairApp.instance.jSOnEvent//JSONObject()
            props.put(
                "utm_source",
                splitQuery(referrerString)
                    .get("utm_source"),
            )
            props.put(
                "utm_medium",
                splitQuery(referrerString)
                    .get("utm_medium"),
            )
            if (splitQuery(referrerString).get("utm_campaign") != null) {
                props.put(
                    "utm_campaign",
                    splitQuery(referrerString).get("utm_campaign"),
                )
            }
            LawnchairApp.instance?.mp?.track("Referral Campaign", props)
            LawnchairApp.instance?.mp?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(LawnchairApp.TAG,"Error ${e.localizedMessage} , ${e.printStackTrace()}")
        }

    }

    @Throws(UnsupportedEncodingException::class)
    fun splitQuery(url: String): Map<String, String> {
        val query_pairs: MutableMap<String, String> = LinkedHashMap()
        val pairs = url.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            query_pairs[URLDecoder.decode(pair.substring(0, idx), "UTF-8")] =
                URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
        }
        return query_pairs
    }

}
