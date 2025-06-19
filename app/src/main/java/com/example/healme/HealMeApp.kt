package com.example.healme

import android.app.Application
import android.util.Log
import com.instacart.library.truetime.TrueTimeRx
import io.reactivex.schedulers.Schedulers

class HealMeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        TrueTimeRx.build()
            .withConnectionTimeout(30000)
            .withRetryCount(3)
            .initializeRx("time.google.com")
            .subscribeOn(Schedulers.io())
            .subscribe(
                { Log.i("TrueTime", "Initialized successfully") },
                { error -> Log.e("TrueTime", "Initialization failed: ${error.message}") }
            )
    }
}
