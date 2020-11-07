package com.example.focusstart7alt

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var timerService: TimerService
    private var bound: Boolean = false
    private lateinit var mainHandler: Handler
    private lateinit var timer: Runnable

    private var serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(className: ComponentName?) {
            Log.e("MainActivity", "TimerService disconnected")
            bound = false
        }

        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            Log.e("MainActivity", "TimerService connected")
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            bound = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainHandler = Handler(Looper.getMainLooper())

        timer = Runnable {
            mainHandler.post {
                timeTextView.text = timerService.count.toString()
            }
            mainHandler.postDelayed(timer, 100)
        }

        val intent = Intent(this, TimerService::class.java)

        startButton.setOnClickListener {
            if (!bound) {
                startService(intent)
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                mainHandler.postDelayed(timer, 500)
            }
        }
    }
}