package fr.chesspackcompose.app.core.network.env

import io.ktor.http.URLProtocol


interface Environment {
    companion object {
        val default: Environment = Dev
    }

    val host: String
    val port: Int
    val protocol: URLProtocol
}

object Local : Environment {
    override val host = "10.0.2.2"
    override val port = 8080
    override val protocol = URLProtocol.WS
}

object Dev : Environment {
    override val host = "chesspack-71354d94eb2f.herokuapp.com"
    override val port = 443
    override val protocol = URLProtocol.WSS
}