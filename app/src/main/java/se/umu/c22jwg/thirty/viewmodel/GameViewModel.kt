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
    private val dieSet: DieSet = state.get<DieSet>("dieSet") ?: DieSet()

    // Store the scoreboard in the state
    val scoreBoard: LiveData<ScoreBoard> = state.getLiveData("scoreBoard", ScoreBoard())

    // LiveData and state handling for important game data
    val round: LiveData<Int> = state.getLiveData("round", 1)
    val roll: LiveData<Int> = state.getLiveData("roll", 0)
    val score: LiveData<Int> = state.getLiveData("score", 0)
    val rollButtonEnabled: LiveData<Boolean> = state.getLiveData("rollButtonEnabled", true)
    val showFinish: LiveData<Boolean> = state.getLiveData("showFinish", false)
    val navigateToResult: LiveData<Boolean> = state.getLiveData("navigateToResult", false)
    val remainingChoices: LiveData<MutableList<String>> =
        state.getLiveData("remainingChoices", allChoices.toMutableList())

    // Init the selected choice to the first choice in the list
    private var selectedChoice: String = remainingChoices.value?.first() ?: ""

    // LiveData that we don't need to save in the state
    private val _nextButtonEnabled = MutableLiveData(true);

    private val _isDieSelectionEnabled = MutableLiveData(roll.value != 0)
    val isDieSelectionEnabled: LiveData<Boolean> = _isDieSelectionEnabled

    val nextButtonEnabled: LiveData<Boolean> = _nextButtonEnabled
    private val _dice = MutableLiveData(dieSet.getDiceSet)
    val dice: LiveData<List<Die>> = _dice

    // Update the live data and state for the dice
    private fun updateDiceState() {
        _dice.value = dieSet.getDiceSet
        state["dieSet"] = dieSet
    }

    fun handleRoll() {
        val currentRoll = roll.value ?: 0

        if (currentRoll < 2) {
            // First or second roll
            if (currentRoll == 0){
                // Now we can enable the die selection
                _isDieSelectionEnabled.value = true
            }
            if (dieSet.getSelected().contains(true)) {
                rollOtherDice()
            } else {
                rollDice()
            }
            state["roll"] = currentRoll + 1
        } else {
            // Third roll
            state["rollButtonEnabled"] = false
            if (dieSet.getSelected().contains(true)) {
                rollOtherDice()
            } else {
                rollDice()
            }
            // Increment roll so it says 3/3
            state["roll"] = currentRoll + 1
        }
    }

    /**
     * Handle the next press meaning the user want to register the current choice and move to the
     * next round. This function call helper functions to calculate the score and update the all
     * relevant data.
     *
     * @param selectedChoice The choice the user made
     * @return Nothing
     */
    fun handleNext() {
        //Reset the roll count and die selection
        state["roll"] = 0
        _isDieSelectionEnabled.value = false
        // TODO Calculate score based on selected choice
        calculateScore()
        Log.d("GameViewModel", "Selected choice: $selectedChoice")
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

    /**
     * Calculate low score
     * @return The score if valid, null otherwise
     */
   fun getLowScore(): Int? {
       var score = 0
       for (die in dieSet.getDiceSet) {
           if (die.selected) {
               if (die.value > 3) {
                   // Invalid selection
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
     * @param current, the current combination
     * @param remaining, the remaining unused dice values
     * @param target, the target sum selected by the player choice
     * @return true if a valid combination is found, false otherwise
     */
    fun findCombination(current: MutableList<Int>, remaining: MutableList<Int>, target: Int): Boolean {
        // Base case, valid combination
        if (target == 0) return true
        // Invalid case, no valid combination or remaining dice
        if (target < 0 || remaining.isEmpty()) return false

        // Iterate and check all remaining dice combinations
        for (i in remaining.indices) {
            //get the value and remove it from the list to make the recursive call
            val value = remaining[i]
            val newRemaining = remaining.toMutableList()
            newRemaining.removeAt(i)
            current.add(value)
            //Check if the recursive call contains a valid combination
            if (findCombination(current, newRemaining, target - value)) {
                return true
            }
            // Backtrack to undo last step that did not lead to a valid combination
            current.removeAt(current.size - 1)
        }
        // No valid combination found
        return false
    }
    /**
     * Calculate the score based on the selected choice and die. It
     * checks if the user has made the correct choice and selected
     * valid combinations.
     *
     * @param choice The choice the user made in the range of 4-12
     * @return The score if valid, null otherwise
     */
    fun getCombinationScore(choice: Int): Int? {
        // Get the values of the selected die
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
                // Remove used dice
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
     * Calculate the score based on the selected choice and die
     */
   fun calculateScore(){
       // reset score
       state["score"] = 0
       // Calculate score based on selected choice
        val score = if (selectedChoice == "Low") {
            getLowScore()
        } else {
            getCombinationScore(selectedChoice.toInt())
        }
        if (score == null) {
            // Invalid selection, notify the user and disable next button
            Log.d("GameViewModel", "Invalid selection")
            //disable the next button
            _nextButtonEnabled.value = false
            return

        }
        // Enable the next button
        _nextButtonEnabled.value = true
        Log.d("GameViewModel", "Score: $score")
        state["score"] = score
    }

    fun onSpinnerChoiceChanged(choice: String) {
        selectedChoice = choice
        calculateScore()
        Log.d("GameViewModel", "Selected choice: $selectedChoice")
        // i dont know if i need to save this in the state lets see
    }


    fun handleFinish() {
        state["navigateToResult"] = true
    }

    fun onNavigatedToResult() {
        state["navigateToResult"] = false
    }

    fun toggleSelected(index: Int) {
        dieSet.toggleSelected(index)
        updateDiceState()
        calculateScore()
    }

    fun resetSelection() {
        dieSet.resetSelection()
        updateDiceState()
        calculateScore()
        state["rollButtonEnabled"] = true
    }

    fun rollDice() {
        if (dieSet.getSelected().contains(true)) {
            rollOtherDice()
            return
        }
        dieSet.rollDice()
        updateDiceState()
    }

    fun rollOtherDice() {
        dieSet.rollOtherDice()
        updateDiceState()
    }

    fun addScoreToBoard(choice: String, score: Int) {
        val board = state["scoreBoard"] ?: ScoreBoard()
        board.addScore(choice, score)
        state["scoreBoard"] = board
    }

}