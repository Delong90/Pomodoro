package com.example.pomodoro

interface TimerListener {
    fun start(id: Int, current: Long)

    fun stop(id: Int, currentMs: Long, currentMsStart: Long, current: Long, numberOfOperations:Int)

    fun delete(id: Int)
}