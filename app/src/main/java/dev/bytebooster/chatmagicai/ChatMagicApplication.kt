package dev.bytebooster.chatmagicai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


open class BaseApp : Application() {

}

@HiltAndroidApp
class ChatMagicApplication : BaseApp() {

}