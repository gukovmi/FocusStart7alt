package com.example.focusstart7alt

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TimerService : Service() {
    private lateinit var executorService: ExecutorService
    private val binder = TimerBinder()
    private lateinit var timer: Runnable
    var count: Int = 0
        private set

    inner class TimerBinder : Binder() {
        fun getService(): TimerService {
            return this@TimerService
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("TimerService", "Created")

        executorService = Executors.newFixedThreadPool(1)

        timer = Runnable {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    TimeUnit.SECONDS.sleep(1)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
                count++
                Log.e("Timer", count.toString())
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        executorService.execute(timer)
        Log.e("TimerService", "Command has been started")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.e("TimerService", "Bound")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.e("TimerService", "Unbound")
        sendNotification(count)
        executorService.shutdownNow()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.e("TimerService", "Destroyed")
        super.onDestroy()
    }

    private fun sendNotification(finishCount: Int) {
        val countData = workDataOf("count" to finishCount)
        val notificationWork = OneTimeWorkRequestBuilder<NotifyWork>()
            .setInitialDelay(5000, TimeUnit.MILLISECONDS)
            .setInputData(countData)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "Timer",
            ExistingWorkPolicy.REPLACE, notificationWork
        )
    }
}