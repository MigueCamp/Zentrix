package com.zentrix.agent

import android.app.Application
import com.zentrix.agent.sync.TokenStore

class ZentrixAgentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)
    }
}
