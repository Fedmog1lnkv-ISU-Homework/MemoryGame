package com.fedmog1lnkv.memorygame.model

sealed class GameState {
    object Waiting : GameState()
    class InGame(val time: Int) : GameState()
    class Win(val time: Int) : GameState()
}