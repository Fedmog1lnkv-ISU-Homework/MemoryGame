package com.fedmog1lnkv.memorygame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fedmog1lnkv.memorygame.databinding.ActivityMainBinding
import com.fedmog1lnkv.memorygame.model.CardModel
import com.fedmog1lnkv.memorygame.model.GameModel
import com.fedmog1lnkv.memorygame.model.GameState
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val gameModel = GameModel(coroutineScope = lifecycleScope)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gameModel.watchFieldChanged().collect {
                    drawField(it)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gameModel.gameState.collect {
                    when (it) {
                        is GameState.InGame -> {
                            binding.winTextView.text = ""
                            binding.timeTextView.text = it.time.toString()
                        }

                        is GameState.Win -> {
                            binding.winTextView.text = resources.getString(R.string.win_text)
                            binding.timeTextView.text = it.time.toString()
                        }

                        GameState.Waiting -> {
                            binding.winTextView.text = resources.getString(R.string.press_to_start)
                            binding.timeTextView.text = "0"
                        }
                    }
                }
            }
        }

        gameModel.generateField()

        binding.field.onCardClickListener = {
            gameModel.openCard(it.id)
        }
    }

    private fun drawField(fieldModel: List<CardModel>) {
        binding.apply {
            field.field = fieldModel
        }
    }
}