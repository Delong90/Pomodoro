package com.example.pomodoro

interface TimerListener {
    fun start(id: Int)

    fun stop(id: Int, currentMs: Long, current: Long, numberOfOperations:Int)

    fun delete(id: Int)
}