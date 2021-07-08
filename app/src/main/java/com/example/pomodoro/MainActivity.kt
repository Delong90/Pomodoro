package com.example.pomodoro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
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
                timers.add(Timer(nextId++,
                    binding.editTextNumber.text.toString().toLong() * 60000,
                    binding.editTextNumber.text.toString().toLong() * 60000,
                    false,
                    0L))
                timerAdapter.submitList(timers.toList())
                binding.editTextNumber.text = null
            }else   {
                Toast.makeText(this, "Invalid input!", Toast.LENGTH_LONG).show()
            }
        }

    }
    override fun start(id: Int,current: Long) {
        changeStopwatch(id, null,null, true,current)
    }

    override fun stop(id: Int, currentMs: Long, currentMsStart: Long,current: Long) {
        changeStopwatch(id, currentMs, currentMsStart, false,current)
    }
    override fun delete(id: Int) {
        timers.remove(timers.find { it.id == id })
        timerAdapter.submitList(timers.toList())
    }
    private fun changeStopwatch(id: Int, currentMs: Long?,currentMsStart: Long?, isStarted: Boolean,current: Long) {
        val newTimers = mutableListOf<Timer>()
        timers.forEach {
            if (it.id == id) {
                newTimers.add(Timer(it.id,
                    currentMs ?: it.currentMs,
                    currentMsStart ?: it.currentMsStart,
                    isStarted,
                    current?: it.current))
            } else {
                newTimers.add(it)
            }
        }
        timerAdapter.submitList(newTimers)
        timers.clear()
        timers.addAll(newTimers)
    }

    private fun checkToast(timerTime: String): Boolean{
        if (timerTime == "") return false
        if (timerTime.toLong() > 6001) return false

        return true
    }


}

