package fr.chesspackcompose.app.core.network.di

import fr.chesspackcompose.app.core.network.WebSocketClient
import fr.chesspackcompose.app.core.network.core.WebSocketClientImpl
import fr.chesspackcompose.app.core.network.env.Environment
import fr.chesspackcompose.app.match_making.domain.MatchMakingStatus
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.core.module.Module
import org.koin.dsl.module

val networkModule: Module
    get() = module {
        single<HttpClient> {
            HttpClient {
                install(plugin = WebSockets) {
                    contentConverter = KotlinxWebsocketSerializationConverter(Json {
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                        serializersModule = SerializersModule {
                            polymorphic(MatchMakingStatus::class) {
                                subclass(MatchMakingStatus.MatchMakingInProgress::class)
                                subclass(MatchMakingStatus.Done::class)
                            }
                        }
                    })
                }
            }
        }
        single<WebSocketClient> {
            WebSocketClientImpl(httpClient = get(), environment = Environment.default)
        }
    }