package fr.chesspackcompose.app.core.logger

private const val defaultTag = "Chesspack"

expect object Logger {
    fun e(message: String, tag: String = defaultTag, throwable: Throwable? = null)
    fun d(message: String, tag: String = defaultTag)
    fun i(message: String, tag: String = defaultTag)
}
