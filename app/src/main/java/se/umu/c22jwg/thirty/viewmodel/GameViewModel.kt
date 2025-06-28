package se.umu.c22jwg.thirty.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import se.umu.c22jwg.thirty.model.Die
import se.umu.c22jwg.thirty.model.DieSet
import se.umu.c22jwg.thirty.model.GameState
import se.umu.c22jwg.thirty.model.ScoreBoard

class GameViewModel(private val state: SavedStateHandle) : ViewModel() {
    
    // Single source of truth for game state
    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState
    
    // Initialize game state from saved state or create new one
    init {
        val savedState = state.get<GameState>("gameState")
        _gameState.value = savedState ?: GameState()
    }
    
    // LiveData properties derived from GameState
    val dice: LiveData<List<Die>> = MutableLiveData<List<Die>>().apply {
        value = _gameState.value?.dieSet?.getDiceSet ?: emptyList()
    }
    
    val round: LiveData<Int> = MutableLiveData<Int>().apply {
        value = _gameState.value?.currentRound ?: 1
    }
    
    val roll: LiveData<Int> = MutableLiveData<Int>().apply {
        value = _gameState.value?.currentRoll ?: 0
    }
    
    val score: LiveData<Int> = MutableLiveData<Int>().apply {
        value = _gameState.value?.currentScore ?: 0
    }
    
    val rollButtonEnabled: LiveData<Boolean> = MutableLiveData<Boolean>().apply {
        value = _gameState.value?.rollButtonEnabled ?: true
    }
    
    val nextButtonEnabled: LiveData<Boolean> = MutableLiveData<Boolean>().apply {
        value = _gameState.value?.nextButtonEnabled ?: true
    }
    
    val showFinish: LiveData<Boolean> = MutableLiveData<Boolean>().apply {
        value = _gameState.value?.showFinish ?: false
    }
    
    val navigateToResult: LiveData<Boolean> = MutableLiveData<Boolean>().apply {
        value = _gameState.value?.navigateToResult ?: false
    }
    
    val remainingChoices: LiveData<MutableList<String>> = MutableLiveData<MutableList<String>>().apply {
        value = _gameState.value?.remainingChoices ?: mutableListOf()
    }
    
    val isDieSelectionEnabled: LiveData<Boolean> = MutableLiveData<Boolean>().apply {
        value = _gameState.value?.isDieSelectionEnabled ?: false
    }

    // Update all LiveData when game state changes
    private fun updateLiveData() {
        val currentState = _gameState.value ?: return
        
        (dice as MutableLiveData).value = currentState.dieSet.getDiceSet
        (round as MutableLiveData).value = currentState.currentRound
        (roll as MutableLiveData).value = currentState.currentRoll
        (score as MutableLiveData).value = currentState.currentScore
        (rollButtonEnabled as MutableLiveData).value = currentState.rollButtonEnabled
        (nextButtonEnabled as MutableLiveData).value = currentState.nextButtonEnabled
        (showFinish as MutableLiveData).value = currentState.showFinish
        (navigateToResult as MutableLiveData).value = currentState.navigateToResult
        (remainingChoices as MutableLiveData).value = currentState.remainingChoices
        (isDieSelectionEnabled as MutableLiveData).value = currentState.isDieSelectionEnabled
    }

    // Update game state and persist it
    private fun updateGameState(newState: GameState) {
        _gameState.value = newState
        state["gameState"] = newState
        updateLiveData()
    }

    fun handleRoll() {
        val currentState = _gameState.value ?: return
        val currentRoll = currentState.currentRoll

        if (currentRoll < 2) {
            // First or second roll
            val newDieSet = currentState.dieSet
            if (currentRoll == 0) {
                // Enable die selection after first roll
                val updatedState = currentState.copyWith(
                    currentRoll = currentRoll + 1,
                    isDieSelectionEnabled = true
                )
                updateGameState(updatedState)
            } else {
                // Second roll
                if (newDieSet.getSelected().contains(true)) {
                    newDieSet.rollOtherDice()
                } else {
                    newDieSet.rollDice()
                }
                val updatedState = currentState.copyWith(
                    dieSet = newDieSet,
                    currentRoll = currentRoll + 1
                )
                updateGameState(updatedState)
            }
        } else {
            // Third roll
            val newDieSet = currentState.dieSet
            if (newDieSet.getSelected().contains(true)) {
                newDieSet.rollOtherDice()
            } else {
                newDieSet.rollDice()
            }
            val updatedState = currentState.copyWith(
                dieSet = newDieSet,
                currentRoll = currentRoll + 1,
                rollButtonEnabled = false
            )
            updateGameState(updatedState)
        }
    }

    fun handleNext() {
        val currentState = _gameState.value ?: return
        
        // Calculate and add score
        val calculatedScore = calculateScore(currentState)
        val newScoreBoard = currentState.scoreBoard
        newScoreBoard.addScore(currentState.selectedChoice, calculatedScore)
        
        // Remove selected choice from remaining choices
        val newRemainingChoices = currentState.remainingChoices.toMutableList()
        newRemainingChoices.remove(currentState.selectedChoice)
        
        val currentRound = currentState.currentRound
        val newRound = if (currentRound < 10) currentRound + 1 else 1
        
        val updatedState = currentState.copyWith(
            scoreBoard = newScoreBoard,
            remainingChoices = newRemainingChoices,
            currentRound = newRound,
            currentRoll = 0,
            currentScore = 0,
            rollButtonEnabled = true,
            isDieSelectionEnabled = false,
            showFinish = newRound == 10,
            nextButtonEnabled = true
        )
        
        // Reset die selection
        updatedState.dieSet.resetSelection()
        
        updateGameState(updatedState)
    }

    fun onSpinnerChoiceChanged(choice: String) {
        val currentState = _gameState.value ?: return
        
        // Only update if the choice actually changed
        if (currentState.selectedChoice == choice) {
            return
        }
        
        Log.d("GameViewModel", "Selected choice: $choice")
        val calculatedScore = calculateScore(currentState.copyWith(selectedChoice = choice))
        
        val updatedState = currentState.copyWith(
            selectedChoice = choice,
            currentScore = calculatedScore,
            nextButtonEnabled = calculatedScore >= 0
        )
        
        updateGameState(updatedState)
    }

    fun toggleSelected(index: Int) {
        val currentState = _gameState.value ?: return
        val newDieSet = currentState.dieSet
        newDieSet.toggleSelected(index)
        
        val calculatedScore = calculateScore(currentState.copyWith(dieSet = newDieSet))
        
        val updatedState = currentState.copyWith(
            dieSet = newDieSet,
            currentScore = calculatedScore,
            nextButtonEnabled = calculatedScore >= 0
        )
        
        updateGameState(updatedState)
    }

    fun handleFinish() {
        val currentState = _gameState.value ?: return
        val newScoreBoard = currentState.scoreBoard
        newScoreBoard.addScore(currentState.selectedChoice, currentState.currentScore)
        
        val updatedState = currentState.copyWith(
            scoreBoard = newScoreBoard,
            navigateToResult = true
        )
        
        updateGameState(updatedState)
    }

    fun onNavigatedToResult() {
        val currentState = _gameState.value ?: return
        val updatedState = currentState.copyWith(navigateToResult = false)
        updateGameState(updatedState)
    }

    fun resetSelection() {
        val currentState = _gameState.value ?: return
        val newDieSet = currentState.dieSet
        newDieSet.resetSelection()
        
        val calculatedScore = calculateScore(currentState.copyWith(dieSet = newDieSet))
        
        val updatedState = currentState.copyWith(
            dieSet = newDieSet,
            currentScore = calculatedScore,
            rollButtonEnabled = true,
            nextButtonEnabled = calculatedScore >= 0
        )
        
        updateGameState(updatedState)
    }

    fun rollDice() {
        val currentState = _gameState.value ?: return
        val newDieSet = currentState.dieSet
        
        if (newDieSet.getSelected().contains(true)) {
            newDieSet.rollOtherDice()
        } else {
            newDieSet.rollDice()
        }
        
        val updatedState = currentState.copyWith(dieSet = newDieSet)
        updateGameState(updatedState)
    }

    fun rollOtherDice() {
        val currentState = _gameState.value ?: return
        val newDieSet = currentState.dieSet
        newDieSet.rollOtherDice()
        
        val updatedState = currentState.copyWith(dieSet = newDieSet)
        updateGameState(updatedState)
    }

    fun gameOver() {
        val resetState = GameState()
        updateGameState(resetState)
    }

    // Score calculation methods (keeping your existing logic)
    private fun calculateScore(gameState: GameState): Int {
        return if (gameState.selectedChoice == "Low") {
            getLowScore(gameState.dieSet) ?: -1
        } else {
            getCombinationScore(gameState.dieSet, gameState.selectedChoice.toInt()) ?: -1
        }
    }

    fun getLowScore(): Int? {
        val currentState = _gameState.value ?: return null
        return getLowScore(currentState.dieSet)
    }

    private fun getLowScore(dieSet: DieSet): Int? {
        var score = 0
        for (die in dieSet.getDiceSet) {
            if (die.selected) {
                if (die.value > 3) {
                    Log.d("GameViewModel", "Invalid selection")
                    return null
                }
                score += die.value
            }
        }
        Log.d("GameViewModel", "Low score: $score")
        return score
    }

    fun findCombination(current: MutableList<Int>, remaining: MutableList<Int>, target: Int): Boolean {
        if (target == 0) return true
        if (target < 0 || remaining.isEmpty()) return false

        for (i in remaining.indices) {
            val value = remaining[i]
            val newRemaining = remaining.toMutableList()
            newRemaining.removeAt(i)
            current.add(value)
            if (findCombination(current, newRemaining, target - value)) {
                return true
            }
            current.removeAt(current.size - 1)
        }
        return false
    }

    fun getCombinationScore(choice: Int): Int? {
        val currentState = _gameState.value ?: return null
        return getCombinationScore(currentState.dieSet, choice)
    }

    private fun getCombinationScore(dieSet: DieSet, choice: Int): Int? {
        val selectedValues = dieSet.getDiceSet
            .filter { it.selected }
            .map { it.value }
            .toMutableList()

        if (selectedValues.isEmpty()) return 0

        var totalScore = 0
        while (true) {
            val tempGroup = mutableListOf<Int>()
            if (findCombination(tempGroup, selectedValues, choice)) {
                tempGroup.forEach { value ->
                    selectedValues.remove(value)
                }
                totalScore += choice
            } else {
                break
            }
        }
        return if (selectedValues.isEmpty() && totalScore > 0) totalScore else null
    }

    fun getRoundResults(): List<Pair<String, Int>> {
        val currentState = _gameState.value ?: return emptyList()
        val scores = currentState.scoreBoard.getScores()
        Log.d("GameViewModel", "Round results: $scores")
        for (score in scores) {
            Log.d("GameViewModel", "Round results: ${score.first} ${score.second}")
        }
        return scores
    }

    fun getTotalScore(): Int {
        val currentState = _gameState.value ?: return 0
        val totalScore = currentState.scoreBoard.getTotalScore()
        Log.d("GameViewModel", "Total score: $totalScore")
        return totalScore
    }
}