package com.example.pomodoro

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),TimerListener {

    private lateinit var binding: ActivityMainBinding

    private val timerAdapter = TimerAdapter(this)
    private val timers = mutableListOf<Timer>()
    private var nextId = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                        false, 0,
                        System.currentTimeMillis(),
                        false
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
        changeStopwatch(id, null, true, null, startTime, true)
    }

    override fun stop(id: Int, currentMs: Long, numberOfOperation: Int, startTime: Long) {
        changeStopwatch(id, currentMs, false, numberOfOperation, startTime, true)
    }

    override fun delete(id: Int) {
        timers.remove(timers.find { it.id == id })
        timerAdapter.submitList(timers.toList())
    }


    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean, numberOfOperation: Int?, startTime: Long?, forcedStop: Boolean?) {
        val newTimers = mutableListOf<Timer>()
        timers.forEach {
            if (it.id == id && isStarted) {
                newTimers.add(
                    Timer(
                        it.id,
                        currentMs ?: it.currentMs,
                        it.currentMsStart,
                        isStarted,
                        numberOfOperation ?: it.numberOfOperation,
                        startTime ?: it.startTime,
                        forcedStop ?: it.forcedStop
                    )
                )
            } else {
                newTimers.add(
                    Timer(
                        it.id,
                        it.currentMs,
                        it.currentMsStart,
                        false,
                        it.numberOfOperation,
                        startTime ?: it.startTime,
                        it.forcedStop
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
}



