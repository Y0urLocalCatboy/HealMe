package com.example.healme

import android.app.Application
import android.util.Log
import com.instacart.library.truetime.TrueTimeRx
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * HealMeApp is the main application class for the HealMe app.
 * It initializes TrueTimeRx for accurate time synchronization.
 */
class HealMeApp : Application() {

    private val disposables = CompositeDisposable()

    /**
     * Called when the application is starting, before any activity, service, or receiver objects
     * (excluding content providers) have been created.
     * This method initializes TrueTimeRx with a specified server and connection settings.
     */
    override fun onCreate() {
        super.onCreate()
        Log.i("TrueTime", "Initializing TrueTimeRx...")
        val trueTimeDisposable = TrueTimeRx.build()
            .withConnectionTimeout(30000)
            .withRetryCount(3)
            .initializeRx("time.google.com")
            .subscribeOn(Schedulers.io())
            .subscribe(
                { Log.i("TrueTime", "Initialized successfully") },
                { error -> Log.e("TrueTime", "Initialization failed: ${error.message}") }
            )

        disposables.add(trueTimeDisposable)
    }

    /**
     * Called when the application is terminating.
     * This method clears all disposables to prevent memory leaks - it may not be needed in this case but - as I know - is a good practice.
     */
    override fun onTerminate() {
        super.onTerminate()
        disposables.clear()
    }
}
