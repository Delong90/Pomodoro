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
        var START_TIME = timer.currentMsStart.displayTime()

        binding.stopwatchTimer.text = (timer.currentMsStart-timer.currentMs).displayTime()

        binding.customView.setPeriod(timer.currentMsStart)
        binding.customView.setCurrent(timer.currentMs)


        if (timer.forcedStart && timer.currentMs == 0L){
            binding.constraintLayout.setBackgroundColor(resources.getColor(R.color.red))
        } else {binding.constraintLayout.setBackgroundColor(resources.getColor(R.color.transparent))
        }


        if (timer.isStarted) {
            binding.startStopButton.text = resources.getText(R.string.stop)
            binding.startStopButton.setBackgroundColor(resources.getColor(R.color.teal_200))
//            binding.deleteButton.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_delete_24_2))

            startTimer(timer)
        }
        else {
            binding.startStopButton.text = resources.getText(R.string.start)
            binding.startStopButton.setBackgroundColor(resources.getColor(R.color.purple_500))
//            binding.deleteButton.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_delete_24))
            stopTimer()
        }


        initButtonsListeners(timer)
    }

    private fun initButtonsListeners(timer: Timer) {
        binding.startStopButton.setOnClickListener {

            if (timer.isStarted) {
                listener.stop(timer.id, timer.currentMs,timer.startTime)
            } else {
                if (!timer.forcedStart){
                    timer.startTime = System.currentTimeMillis()
                } else timer.startTime = System.currentTimeMillis()-timer.currentMs
                listener.start(timer.id,timer.startTime)
            }
        }

        binding.deleteButton.setOnClickListener {
            binding.customView.setCurrent(0L)
            this.countDownTimer?.cancel()
            listener.delete(timer.id) }
    }

    private fun startTimer(timer: Timer) {
        binding.constraintLayout.setBackgroundColor(resources.getColor(R.color.transparent))
        this.countDownTimer?.cancel()
        this.countDownTimer = getCountDownTimer(timer)
        this.countDownTimer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }
    private fun stopTimer() {

        this.countDownTimer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }


    private fun getCountDownTimer(timer: Timer): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_TEN_MS) {

            override fun onTick(millisUntilFinished: Long) {
                binding.customView.setPeriod(timer.currentMsStart)
                binding.customView.setCurrent(timer.currentMs)
                timer.currentMs = (System.currentTimeMillis()-timer.startTime)
                binding.stopwatchTimer.text = (timer.currentMsStart-timer.currentMs).displayTime()
                println("${timer.id} ${timer.currentMs}")


                if (timer.currentMs >= timer.currentMsStart) {
                    timer.currentMs = 0L
                    stopTimer()
                    listener.stop(timer.id, timer.currentMs,timer.startTime)
                }
            }
            override fun onFinish() {
                binding.stopwatchTimer.text = timer.currentMs.displayTime()
            }
        }
    }





    private companion object {

        private const val UNIT_TEN_MS = 10L
        private var PERIOD = 1000L * 60L * 60L * 24L // Day

    }




}


