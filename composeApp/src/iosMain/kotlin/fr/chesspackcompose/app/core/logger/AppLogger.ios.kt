package fr.chesspackcompose.app.core.logger

import platform.Foundation.NSLog

actual object Logger {
    actual fun e(message: String, tag: String, throwable: Throwable?) {
        if (throwable != null) {
            NSLog("ERROR: [$tag] $message. Throwable: $throwable CAUSE ${throwable.cause}")
        } else {
            NSLog("ERROR: [$tag] $message")
        }
    }

    actual fun d(message: String, tag: String) {
        NSLog("DEBUG: [$tag] $message")
    }

    actual fun i(message: String, tag: String) {
        NSLog("INFO: [$tag] $message")
    }
}