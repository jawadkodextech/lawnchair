package app.lawnchair.search.searchsuggestion

import android.util.Log
import app.lawnchair.search.searchsuggestion.Logger


class AndroidLogger() : Logger {

    override fun log(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun log(tag: String, message: String, throwable: Throwable) {
        Log.e(tag, message, throwable)
    }

}
