package com.example.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.TimerItemBinding


class TimerViewHolder(
    private val binding: TimerItemBinding,
    private val listener: TimerListener,
    private val resources: Resources

) : RecyclerView.ViewHolder(binding.root) {

    private var countDownTimer: CountDownTimer? = null

    fun bind(timer: Timer) {
        START_TIME = timer.currentMsStart.displayTime()

        binding.stopwatchTimer.text = timer.currentMs.displayTime()

        binding.customView.setPeriod(timer.currentMsStart)
        binding.customView.setCurrent(timer.current)


        if (timer.numberOfOperation != 0 && timer.currentMs == timer.currentMsStart){
            binding.constraintLayout.setBackgroundColor(resources.getColor(R.color.red))
        } else {binding.constraintLayout.setBackgroundColor(resources.getColor(R.color.transparent))
        }


        if (timer.isStarted) {
            startTimer(timer)
        }
        else {
            stopTimer()
        }


        initButtonsListeners(timer)
    }

    private fun initButtonsListeners(timer: Timer) {
        binding.startStopButton.setOnClickListener {

            if (timer.isStarted) {
                listener.stop(timer.id, timer.currentMs, timer.current,timer.numberOfOperation)
            } else {
                listener.start(timer.id)
            }
        }

        binding.deleteButton.setOnClickListener {
            timer.current = 0L
            binding.customView.setCurrent(timer.current)
            this.countDownTimer?.cancel()
            listener.delete(timer.id) }
    }

    private fun startTimer(timer: Timer) {
        binding.startStopButton.text = resources.getText(R.string.stop)
        binding.constraintLayout.setBackgroundColor(resources.getColor(R.color.transparent))
        this.countDownTimer?.cancel()
        this.countDownTimer = getCountDownTimer(timer)
        this.countDownTimer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }
    private fun stopTimer() {
        binding.startStopButton.text = resources.getText(R.string.start)

        this.countDownTimer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(timer: Timer): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {
            val interval = UNIT_TEN_MS

            override fun onTick(millisUntilFinished: Long) {
                binding.customView.setPeriod(timer.currentMsStart)
                binding.customView.setCurrent(timer.current+1000)
                timer.current += interval + 6000
                timer.currentMs -= interval + 6000
                binding.stopwatchTimer.text = timer.currentMs.displayTime()
                println(timer.currentMs)
                println(timer.id)




                if (timer.currentMs <= 0L) {
                    binding.startStopButton.text = "START"
                    timer.current = 0L
                    binding.customView.setCurrent(timer.current)
                    timer.currentMs = timer.currentMsStart
                    timer.numberOfOperation = timer.numberOfOperation+1
                    stopTimer()
                    listener.stop(timer.id, timer.currentMs,timer.current,timer.numberOfOperation)
                    binding.constraintLayout.setBackgroundColor(resources.getColor(R.color.red))
                }

            }

            override fun onFinish() {
                binding.stopwatchTimer.text = timer.currentMs.displayTime()
            }
        }
    }






//    ______________________________________________________________________________________
//    ______________________________________________________________________________________
//    ______________________________________________________________________________________
//    ______________________________________________________________________________________
//    ______________________________________________________________________________________

    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return START_TIME
        }

        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60


        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"

    }



    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {

        private var START_TIME = "00:00:00"
        private const val UNIT_TEN_MS = 1000L
        private var PERIOD = 1000L * 60L * 60L * 24L // Day

    }




}


