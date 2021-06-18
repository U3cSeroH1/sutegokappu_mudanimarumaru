package com.example.sutegokappu_mudanimarumaru

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityTransitionResult

private const val TAG = "MyBroadcastReceiver"

class MyBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
//        StringBuilder().apply {
//            append("Action: ${intent.action}\n")
//            append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
//            toString().also { log ->
//                Log.e(context.toString(), "log")
//                Toast.makeText(context, log, Toast.LENGTH_LONG).show()
//            }
//        }

        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)!!
            for (event in result.transitionEvents) {
                // chronological sequence of events....
                Log.e("アクティビティ", event.activityType.toString())
            }
        }

        Log.e("アクティビティ", ActivityTransitionResult.hasResult(intent).toString())

    }
}