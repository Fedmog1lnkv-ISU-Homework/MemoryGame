package com.fedmog1lnkv.memorygame.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameModel(private val coroutineScope: CoroutineScope) {
    private val field = mutableListOf<CardModel>()
    private val fieldFlow = MutableStateFlow(listOf<CardModel>())
    fun watchFieldChanged(): StateFlow<List<CardModel>> = fieldFlow

    private val _gameState = MutableStateFlow<GameState>(GameState.Waiting)
    val gameState: StateFlow<GameState> = _gameState

    private var closeCardsJob: Job? = null
    private var timerJob: Job? = null

    fun generateField() {
        field.clear()
        for (i in 0 until 4 * 4) {
            field.add(CardModel(id = i, type = i / 2))
        }
        field.shuffle()
        emitField()
    }

    private fun emitField() {
        fieldFlow.update { field.map { it.copy() }.toList() }
    }

    fun openCard(id: Int) {
        if (_gameState.value is GameState.Waiting) {
            timerJob?.cancel()
            _gameState.update { GameState.InGame(0) }
            timerJob = coroutineScope.launch {
                while (true) {
                    delay(1000)
                    _gameState.update {
                        GameState.InGame(
                            ((it as? GameState.InGame)?.time ?: 0) + 1
                        )
                    }
                }
            }
        }

        val card = field.find { it.id == id } ?: return
        if (card.isOpen || card.isConnected) return

        closeCardsJob?.cancel()
        closeCardsJob = null

        val openedCards = getTempOpenedCards()

        if (openedCards.size == 2) {
            closeAllCards()
        }

        card.isOpen = true

        emitField()

        if (openedCards.size == 1) {
            if (card.type == openedCards.first().type) {
                card.isConnected = true
                openedCards.first().isConnected = true
            } else {
                closeCardsJob = coroutineScope.launch {
                    delay(1000)
                    closeAllCards()
                }
            }
        }

        if (field.all { it.isConnected && it.isOpen }) {
            timerJob?.cancel()
            _gameState.update { GameState.Win((it as? GameState.InGame)?.time ?: 0) }
        }

        emitField()
    }

    private fun getTempOpenedCards(): List<CardModel> =
        field.filter { it.isOpen && !it.isConnected }

    private fun closeAllCards() {
        for (card in field) {
            if (card.isOpen && !card.isConnected) {
                card.isOpen = false
            }
        }
        emitField()
    }
}