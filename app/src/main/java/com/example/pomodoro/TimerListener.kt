package com.example.pomodoro

interface TimerListener {
    fun start(id: Int,startTime: Long)

    fun stop(id: Int, currentMs: Long,startTime: Long)

    fun delete(id: Int)

}