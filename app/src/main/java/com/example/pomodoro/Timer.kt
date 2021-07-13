package com.example.pomodoro

data class Timer(
    val id: Int,
    var currentMs: Long,
    var currentMsStart: Long,
    var isStarted: Boolean,
    var numberOfOperation: Int,
    var startTime: Long,
    var forcedStart: Boolean
)