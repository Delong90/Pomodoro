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
        START_TIME = timer.currentMs.displayTime(timer)
        binding.stopwatchTimer.text = timer.currentMs.displayTime(timer)

        if (timer.isStarted) {
            startTimer(timer)
        } else {
            stopTimer(timer)
        }
        initButtonsListeners(timer)
    }

    private fun initButtonsListeners(timer: Timer) {
        binding.startStopButton.setOnClickListener {

            if (timer.isStarted) {
                listener.stop(timer.id, timer.currentMs, timer.currentMsStart, timer.current)
            } else {
                listener.start(timer.id,timer.current)
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

        this.countDownTimer?.cancel()
        this.countDownTimer = getCountDownTimer(timer)
        this.countDownTimer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }
    private fun stopTimer(timer: Timer) {
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
                binding.customView.setCurrent(timer.current)
                timer.current += interval + 300
                timer.currentMs -= interval + 300
                binding.stopwatchTimer.text = timer.currentMs.displayTime(timer)
            }

            override fun onFinish() {
                binding.stopwatchTimer.text = timer.currentMs.displayTime(timer)
            }
        }
    }

    private fun Long.displayTime(timer: Timer): String {
        if (this <= 0L) {
            binding.startStopButton.text = "START"
            timer.current = 0L
            timer.currentMs = timer.currentMsStart
            listener.stop(timer.id, timer.currentMs, timer.currentMsStart,timer.current)
            return START_TIME
        }

        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60
        val ms = this % 1000 / 10

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}:${displaySlot(ms)}"

    }



    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {

        private var START_TIME = "00:00:00:00"
        private const val UNIT_TEN_MS = 10L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day

    }


}


