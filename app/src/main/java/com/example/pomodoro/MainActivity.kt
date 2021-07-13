package com.example.pomodoro

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding
import androidx.lifecycle.*

class MainActivity : AppCompatActivity(),TimerListener,LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    private val timerAdapter = TimerAdapter(this)
    private val timers = mutableListOf<Timer>()
    private var nextId = 0
    private var startTimeNotification = 0L



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timerAdapter
        }

        binding.addNewTimerButton.setOnClickListener {
            if (checkToast(binding.editTextNumber.text.toString())) {
                timers.add(
                    Timer(
                        nextId++,
                        0L,
                        binding.editTextNumber.text.toString().toLong() * 60000,
                        false,
                        0L,
                        forcedStart = false
                    )
                )
                timerAdapter.submitList(timers.toList())
//                binding.editTextNumber.text = null
            } else {
                Toast.makeText(this, "Invalid input!", Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun start(id: Int, startTime: Long) {
        changeStopwatch(id, null, true, startTime)
    }

    override fun stop(id: Int, currentMs: Long, startTime: Long) {
        startTimeNotification = 0L
        changeStopwatch(id, currentMs, false, startTime)
    }



    override fun delete(id: Int) {
        val timerDelete = timers.find { it.id == id }
        if(timerDelete!!.isStarted) startTimeNotification = 0L
        timers.remove(timers.find { it.id == id })
        timerAdapter.submitList(timers.toList())
    }


    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean, startTime: Long) {
        val newTimers = mutableListOf<Timer>()
        timers.forEach {
            if (it.isStarted && it.id!=id){
                val currentMsNew = System.currentTimeMillis()-it.startTime
                newTimers.add(
                    Timer(
                        it.id,
                        currentMsNew,
                        it.currentMsStart,
                        false,
                        startTime,
                        true
                    )
                )
            } else if (it.id == id && isStarted) {
                startTimeNotification = it.currentMsStart + startTime
                println("it.currentMs + it.startTime ${it.currentMs} + ${it.startTime}")
                newTimers.add(
                    Timer(
                        it.id,
                        currentMs ?: it.currentMs,
                        it.currentMsStart,
                        isStarted,
                        startTime,
                        true
                    )
                )
            } else {
                newTimers.add(
                    Timer(
                        it.id,
                        it.currentMs,
                        it.currentMsStart,
                        false,
                        startTime,
                        it.forcedStart
                    )
                )
            }

        }
        timerAdapter.submitList(newTimers)
        timers.clear()
        timers.addAll(newTimers)
        println("список таймеров после старт $timers")
    }


    private fun checkToast(timerTime: String): Boolean {
        if (timerTime == "") return false
        if (timerTime.toLong() > 6001) return false

        return true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.putExtra(COMMAND_ID, COMMAND_START)
        startIntent.putExtra(STARTED_TIMER_TIME_MS, startTimeNotification)
        startService(startIntent)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }
}



