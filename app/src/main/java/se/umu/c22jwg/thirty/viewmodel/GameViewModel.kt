package se.umu.c22jwg.thirty.viewmodel
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import se.umu.c22jwg.thirty.model.DieSet
import se.umu.c22jwg.thirty.model.GameState
import androidx.lifecycle.map

class GameViewModel(private val state: SavedStateHandle) : ViewModel() {

    private val _gameState = MutableLiveData<GameState>()

    // Init the game state from saved state or create a new one
    init {
        val savedState = state.get<GameState>("gameState")
        _gameState.value = savedState ?: GameState()

    }
    // Expose the game state as LiveData with transformations for UI binding
    val dice = _gameState.map { it.dieSet.getDiceSet }
    val round = _gameState.map { it.currentRound }
    val roll = _gameState.map { it.currentRoll }
    val score = _gameState.map { it.currentScore }
    val rollButtonEnabled = _gameState.map { it.rollButtonEnabled }
    val nextButtonEnabled = _gameState.map { it.nextButtonEnabled }
    val showFinish = _gameState.map { it.showFinish }
    val navigateToResult = _gameState.map { it.navigateToResult }
    val remainingChoices = _gameState.map { it.remainingChoices }
    val isDieSelectionEnabled = _gameState.map { it.isDieSelectionEnabled }
    val selectedChoice = _gameState.map { it.selectedChoice }

    /**
     * Update and save the new game state in the SavedStateHandle.
     *
     * @param newState, the new game state to be saved.
     */
    private fun updateGameState(newState: GameState) {
        _gameState.value = newState
        state["gameState"] = newState
    }

    /**
     * Handles the roll button click event. Checks amount of rolls, rolls the
     * dice and updates the game state.
     */
    fun handleRoll() {
        //testCombinationScore()
        val currentState = _gameState.value ?: return
        val currentRoll = currentState.currentRoll

        if (currentRoll < 2) {
            // First or second roll
            val newDieSet = currentState.dieSet
            if (currentRoll == 0) {
                // Roll all dice
                newDieSet.rollDice()
                val updatedState = currentState.copy(
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
                val updatedState = currentState.copy(
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
            val updatedState = currentState.copy(
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
        val updatedState = currentState.copy(
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
        val calculatedScore = calculateScore(currentState.copy(selectedChoice = choice))

        val updatedState = currentState.copy(
            selectedChoice = choice,
            currentScore = if (calculatedScore == -1)  0 else calculatedScore,
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
        val calculatedScore = calculateScore(currentState.copy(dieSet = newDieSet))
        val updatedState = currentState.copy(
            dieSet = newDieSet,
            currentScore = if (calculatedScore == -1)  0 else calculatedScore,
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

        val updatedState = currentState.copy(
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
            getLowScore(gameState.dieSet)
        } else {
            getCombinationScore(gameState.dieSet, gameState.selectedChoice.toInt())
        }
    }

    /**
     * Calculate the low score, the sum of all dice with a value less than 4.
     *
     * @param dieSet, the set of dice to be used in the calculation
     * @return The score if valid, -1 otherwise
     */
    private fun getLowScore(dieSet: DieSet): Int {
        var score = 0
        for (die in dieSet.getDiceSet) {
            if (die.selected) {
                if (die.value > 3) {
                    Log.d("GameViewModel", "Invalid selection")
                    return -1
                }
                score += die.value
            }
        }
        Log.d("GameViewModel", "Low score: $score")
        return score
    }

    /**
     * Calculates the maximum score possible for the selected choice.
     *
     * @param dieSet, the set of dice to be used in the calculation
     * @param choice, the selected choice for the calculation
     * @return the calculated score or -1 if the selection is invalid
     */
    fun getCombinationScore(dieSet: DieSet, choice: Int): Int {
        // Get the selected dice values
        val selectedValues = dieSet.getDiceSet
            .filter { it.selected }
            .map { it.value }
            .toMutableList()
        // No die selected, 0 points
        if (selectedValues.isEmpty()) return 0
        // Get all grouping combinations of the selected dice
        val allCombos =getAllCombinations(selectedValues,choice)
        // Pick the combinationes with least dice used and highest score
        val maxScore = findBestCombination(selectedValues, allCombos,choice)
        // Return the max score if valid
        return if (maxScore >= 0) maxScore else -1
    }

    /**
     * This method gets all possible combinations of the given dice and the target value.
     * It uses backtracking to generate all possible combinations for the selected dice
     * that sums to the selected scoring choice.
     *
     * @param dice, the list of dice to be used in the calculation
     * @param choice, the choice selected by the user
     * @return  a list of all valid combinations where each combination is a list of dice values
     */
    private fun getAllCombinations(dice: List<Int>, choice: Int): List<List<Int>> {
        // Sort the dice in ascending order
        val result = mutableListOf<List<Int>>()
        val sortedDice = dice.sorted()

        // Backtracking function to generate combinations
        fun backtrack(start: Int, path: MutableList<Int>, sum: Int) {
            // Add the current combination if it is valid
            if (sum == choice) {
                Log.d("GameViewModelComb", "Combination found: $path sum: $sum choice: $choice")
                result.add(path.toList())
                return
            }
            // Return if the the current sum is bigger
            if (sum > choice) {
                Log.d("GameViewModelComb", "Combination not found: $path sum: $sum choice: $choice")
                return
            }
            // Iterate through the sorted dice
            for (i in start until sortedDice.size) {
                // Avoid generating duplicate combinations in different orders
                if (i > start && sortedDice[i] == sortedDice[i - 1]) continue
                // Add the current die to the path and get the new sum
                path.add(sortedDice[i])
                // Recursively generate the next combination
                backtrack(i + 1, path, sum + sortedDice[i])
                // Now remove last die to check other combinations
                path.removeAt(path.lastIndex)
            }
            // No combination found
            return
        }
        // Start the backtracking
        backtrack(0, mutableListOf(), 0)
        return result
    }

    /**
     * Finds the maximum total score by selecting combinations that dont overlap from the list.
     * Each combination uses distinct dice by tracking which dice indices are already used.
     * This ensures that even if dice values are the same, dice are only used once per combination.
     *
     * @param dice, the list of dice to be used in the calculation
     * @param combinations, the list of all possible combinations
     * @param choice, the choice selected by the user
     * @return the maximum total score by using combinations that dont overlap
     */
    private fun findBestCombination(dice: List<Int>, combinations: List<List<Int>>, choice: Int): Int {
        var maxScore = -1
        // Recursively backtrack through the combinations
        fun backtrack(used: MutableList<Boolean>, count: Int) {
            var foundCombo = false
            // Iterate over each found combination
            for (combo in combinations) {
                val tempUsed = used.toMutableList()
                var valid = true
                // Check that the dice in the combination are not used in other combinations
                for (dieVal in combo) {
                    // Find the index of the die in the dice list where it is not used
                    val index = tempUsed.withIndex().indexOfFirst { (i, isUsed) -> !isUsed && dice[i] == dieVal }
                    // No index means invalid combination since the die already used
                    if (index == -1) {
                        valid = false
                        break
                    }
                    // Mark the die as used
                    tempUsed[index] = true
                }
                // If the combination is valid, check if it is the best combination
                if (valid) {
                    foundCombo = true
                    backtrack(tempUsed, count + 1)
                }
            }
            //If no more valid combos to add, check if al dice are used and update max score
            if (!foundCombo) {
                val usedCount = used.count { it }
                if (usedCount == dice.size && count > 0) {
                    // Save the highest score, either the previous or the current combination
                    maxScore = maxOf(maxScore, count * choice)
                }
            }
        }
        // Start the backtracking, with no dice used and  no groups
        backtrack(MutableList(dice.size) { false }, 0)
        return maxScore
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

    /**
     * Function to test the getCombinationScore function
     */
    fun testCombinationScore() {
        val testDieSet = DieSet()
        val values = listOf(1, 1, 3, 5, 5, 5)

        for ((i, die) in testDieSet.getDiceSet.withIndex()) {
            die.value = values[i]
            die.selected = true
        }

        val score = getCombinationScore(testDieSet, 10)
        Log.d("GameViewModelComb", "Score: $score")
    }
}