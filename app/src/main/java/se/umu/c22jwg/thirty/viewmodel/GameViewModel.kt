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
    
    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState
    
    // Init the game state from saved state or create a new one
    init {
        val savedState = state.get<GameState>("gameState")
        _gameState.value = savedState ?: GameState()
    }
    
    // LiveData for the UI to observe
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

    /**
     * Updates the LiveData values each time the game state changes.
     */
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

    /**
     * Update and save the new game state in the SavedStateHandle.
     *
     * @param newState, the new game state to be saved.
     */
    private fun updateGameState(newState: GameState) {
        _gameState.value = newState
        state["gameState"] = newState
        updateLiveData()
    }

    /**
     * Handles the roll button click event. Checks amount of rolls, rolls the
     * dice and updates the game state.
     */
    fun handleRoll() {
        val currentState = _gameState.value ?: return
        val currentRoll = currentState.currentRoll

        if (currentRoll < 2) {
            // First or second roll
            val newDieSet = currentState.dieSet
            if (currentRoll == 0) {
                // Roll all dice
                newDieSet.rollDice()
                val updatedState = currentState.copyWith(
                    dieSet = newDieSet,
                    currentRoll = 1,
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

    /**
     * Handles the next button click event. Uses helper methods to calculate the
     * score based on the users choice selection and update the game state.
     */
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
        // Update the state for the next round
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

    /**
     * Handles the spinner choice change event. Updates the game state with the
     * selected choice.
     *
     * @param choice, the selected choice from the spinner.
     */
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

    /**
     * Handles the die selection event, it toggles the selected die based
     * on the index. Also it updates the game score so the user can see
     * live score updates when selecting dice.
     */
    fun toggleSelected(index: Int) {
        val currentState = _gameState.value ?: return
        val newDieSet = currentState.dieSet
        newDieSet.toggleSelected(index)
        // Update the score
        val calculatedScore = calculateScore(currentState.copyWith(dieSet = newDieSet))
        val updatedState = currentState.copyWith(
            dieSet = newDieSet,
            currentScore = calculatedScore,
            nextButtonEnabled = calculatedScore >= 0
        )
        updateGameState(updatedState)
    }

    /**
     * Handles the finish button click, it updates the state with the score from
     * the last round and navigates to the result screen.
     */

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


    /**
     * Calculate the score based on the users choice selection.
     *
     * @param gameState, the current game state
     * @return the calculated score or 0 if the selection is invalid
     */
    private fun calculateScore(gameState: GameState): Int {
        return if (gameState.selectedChoice == "Low") {
            getLowScore(gameState.dieSet) ?: 0
        } else {
            getCombinationScore(gameState.dieSet, gameState.selectedChoice.toInt()) ?: 0
        }
    }

    /**
     * Calculate the low score, the sum of all dice with a value less than 4.
     *
     * @param dieSet, the set of dice to be used in the calculation
     * @return The score if valid, null otherwise
     */
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

    /**
     * Recursive helper function to find any combination of in remaining  that sums to target
     * When a valid combination is found, it is added it gets stored in current.
     *
     * @param current, the current combination being built
     * @param remaining, the remaining values to be used in the combination
     * @param target, the target sum of the combination
     * @return true if a valid combination is found, false otherwise
     */
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

    /**
     * Calculates the score based on the selected die combination and
     * the selected choice. It uses findCombination to find all valid
     * combinations and adds the score for each valid combination.
     *
     * @param dieSet, the set of dice to be used in the calculation
     * @param choice, the selected choice for the calculation
     * @return the calculated score or null if the selection is invalid
     */
    private fun getCombinationScore(dieSet: DieSet, choice: Int): Int? {
        // Get the selected dice values
        val selectedValues = dieSet.getDiceSet
            .filter { it.selected }
            .map { it.value }
            .toMutableList()
        // No die selected, 0 points
        if (selectedValues.isEmpty()) return 0
        var totalScore = 0
        // Find all valid grouping combinations
        while (true) {
            val tempGroup = mutableListOf<Int>()
            // Add the point each time a combination is found
            if (findCombination(tempGroup, selectedValues, choice)) {
                // Remove used values to avoid duplicates
                tempGroup.forEach { value ->
                    selectedValues.remove(value)
                }
                totalScore += choice
            } else {
                break
            }
        }
        // Return points if valid and no remaining dice
        return if (selectedValues.isEmpty() && totalScore > 0) totalScore else null
    }

    /**
     * Get the results of all rounds from the score board, stored in order
     * of round number.
     *
     * @return a list of pairs containing the round number and the score
     */
    fun getRoundResults(): List<Pair<String, Int>> {
        val currentState = _gameState.value ?: return emptyList()
        val scores = currentState.scoreBoard.getScores()
        Log.d("GameViewModel", "Round results: $scores")
        for (score in scores) {
            Log.d("GameViewModel", "Round results: ${score.first} ${score.second}")
        }
        return scores
    }

    /**
     * Get the total score from the score board
     *
     * @return the total score
     */
    fun getTotalScore(): Int {
        val currentState = _gameState.value ?: return 0
        val totalScore = currentState.scoreBoard.getTotalScore()
        Log.d("GameViewModel", "Total score: $totalScore")
        return totalScore
    }
    /**
     * Resets the state of the game
     */
    fun gameOver() {
        val resetState = GameState()
        updateGameState(resetState)
    }
}