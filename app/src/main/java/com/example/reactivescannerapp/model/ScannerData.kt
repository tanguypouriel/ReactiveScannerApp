package com.example.reactivescannerapp.model

data class ScannerData(
    var state: State = State.STOP,
    var speed: Int = 3
)

enum class State{
    IS_RUNNING_RIGHT, IS_RUNNING_LEFT, IS_MAX_LEFT, IS_MAX_RIGHT, STOP
}