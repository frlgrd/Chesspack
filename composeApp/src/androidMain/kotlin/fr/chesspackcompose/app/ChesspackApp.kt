package fr.chesspackcompose.app

import android.app.Application
import fr.chesspackcompose.app.core.di.initKoin
import org.koin.android.ext.koin.androidContext

class ChesspackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@ChesspackApp)
        }
    }
}