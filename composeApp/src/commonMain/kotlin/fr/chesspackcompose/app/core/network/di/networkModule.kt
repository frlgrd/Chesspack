package fr.chesspackcompose.app.core.network.di

import fr.chesspackcompose.app.core.network.WebSocketClient
import fr.chesspackcompose.app.core.network.core.WSEventMapper
import fr.chesspackcompose.app.core.network.core.WebSocketClientImpl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

val networkModule: Module
    get() = module {
        single<HttpClient> {
            HttpClient {
                install(plugin = WebSockets) {
                    contentConverter = KotlinxWebsocketSerializationConverter(Json)
                }
            }
        }
        single { WSEventMapper() }
        single<WebSocketClient> { WebSocketClientImpl(httpClient = get(), mapper = get()) }
    }