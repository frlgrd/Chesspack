package fr.chesspackcompose.app.core.logger

import android.util.Log

actual object Logger {

    actual fun e(message: String, tag: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    actual fun d(message: String, tag: String) {
        Log.d(tag, message)
    }

    actual fun i(message: String, tag: String) {
        Log.i(tag, message)
    }
}