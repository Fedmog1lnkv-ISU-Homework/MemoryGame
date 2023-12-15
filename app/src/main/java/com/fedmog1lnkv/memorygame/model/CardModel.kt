package com.fedmog1lnkv.memorygame.model

data class CardModel(
    val id: Int,
    val type: Int,
    var isOpen: Boolean = false,
    var isConnected: Boolean = false,
)