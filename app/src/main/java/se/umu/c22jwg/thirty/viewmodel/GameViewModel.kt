package se.umu.c22jwg.thirty.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import se.umu.c22jwg.thirty.model.Die
import se.umu.c22jwg.thirty.model.DieSet
import se.umu.c22jwg.thirty.model.ScoreBoard

class GameViewModel(private val state: SavedStateHandle) : ViewModel() {
    private val allChoices = listOf("Low", "4", "5", "6", "7", "8", "9", "10", "11", "12")
    private val dieSet = DieSet()

    // Store the scoreboard in the state
    val scoreBoard: LiveData<ScoreBoard> = state.getLiveData("scoreBoard", ScoreBoard())

    // LiveData and state handling for important game data
    val round: LiveData<Int> = state.getLiveData("round", 1)
    val roll: LiveData<Int> = state.getLiveData("roll", 1)
    val score: LiveData<Int> = state.getLiveData("score", 0)
    val showFinish: LiveData<Boolean> = state.getLiveData("showFinish", false)
    val navigateToResult: LiveData<Boolean> = state.getLiveData("navigateToResult", false)
    val remainingChoices: LiveData<MutableList<String>> =
        state.getLiveData("remainingChoices", allChoices.toMutableList())


    private val _rollButtonEnabled = MutableLiveData(true)
    val rollButtonEnabled: LiveData<Boolean> = _rollButtonEnabled

    private val _dice = MutableLiveData(dieSet.getDiceSet)
    val dice: LiveData<List<Die>> = _dice

    fun handleRoll() {
        val currentRoll = roll.value ?: 1

        if (currentRoll < 3) {
            // First or second roll
            if (dieSet.getSelected().contains(true)) {
                rollOtherDice()
            } else {
                rollDice()
            }
            state["roll"] = currentRoll + 1
        } else {
            // Third roll
            _rollButtonEnabled.value = false
            if (dieSet.getSelected().contains(true)) {
                rollSelectedDice()
            } else {
                rollDice()
            }
            state["roll"] = 1
        }
    }

    fun handleNext(selectedChoice: String) {
        // TODO Calculate score based on selected choice

        //Remove the selected choice from the remaining choices list
        val currentList = state.get<MutableList<String>>("remainingChoices") ?: return
        currentList.remove(selectedChoice)
        state["remainingChoices"] = currentList

        val currentRound = round.value ?: 1
        if (currentRound < 10) {
            if (currentRound == 9) {
                state["showFinish"] = true
            }
            state["round"] = currentRound + 1
            resetSelection()
        } else {
            // Game over
            Log.d("GameViewModel", "Game over!")
            state["score"] = 0
            state["showFinish"] = false
            state["round"] = 1
        }
    }

    fun handleFinish() {
        state["navigateToResult"] = true
    }

    fun onNavigatedToResult() {
        state["navigateToResult"] = false
    }

    fun toggleSelected(index: Int) {
        dieSet.toggleSelected(index)
        _dice.value = dieSet.getDiceSet
    }

    fun resetSelection() {
        dieSet.resetSelection()
        _dice.value = dieSet.getDiceSet
        _rollButtonEnabled.value = true
    }

    fun rollDice() {
        if (dieSet.getSelected().contains(true)) {
            rollOtherDice()
            return
        }
        dieSet.rollDice()
        _dice.value = dieSet.getDiceSet
    }

    fun rollSelectedDice() {
        dieSet.rollSelectedDice()
        _dice.value = dieSet.getDiceSet
    }

    fun rollOtherDice() {
        dieSet.rollOtherDice()
        _dice.value = dieSet.getDiceSet
    }

    fun addScoreToBoard(choice: String, score: Int) {
        val board = state["scoreBoard"] ?: ScoreBoard()
        board.addScore(choice, score)
        state["scoreBoard"] = board
    }

}