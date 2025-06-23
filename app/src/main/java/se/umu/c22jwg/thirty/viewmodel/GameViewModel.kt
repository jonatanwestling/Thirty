package se.umu.c22jwg.thirty.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import se.umu.c22jwg.thirty.model.Die
import se.umu.c22jwg.thirty.model.DieSet

class GameViewModel: ViewModel() {
    private val dieSet = DieSet();
    // LiveData for the dice
    private val _dice = MutableLiveData(dieSet.getDiceSet)
    val dice: LiveData<List<Die>> = _dice
    // LiveData for the round
    private val _round = MutableLiveData(1)
    val round: LiveData<Int> = _round
    // LiveData for the roll
    private val _roll = MutableLiveData(1)
    val roll: LiveData<Int> = _roll
    // LiveData for the score
    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score
    // Live data for roll button disable/activated state
    private val _rollButtonEnabled = MutableLiveData(true)
    val rollButtonEnabled: LiveData<Boolean> = _rollButtonEnabled
    fun handleRoll() {
        val currentRoll = _roll.value ?: 1
        val currentRound = _round.value ?: 1

        if (currentRoll < 3) {
            // First or second roll
            if (dieSet.getSelected().contains(true)) {
                rollOtherDice()
            } else {
                // None selected, roll all dice
                rollDice()
            }
            _roll.value = currentRoll + 1
            } else {
                // Third roll
                if (dieSet.getSelected().contains(true)) {
                    rollSelectedDice()
                } else {
                    // None selected, roll all dice
                    rollDice()
                }
                _roll.value = 1
                _round.value = currentRound + 1
                resetSelection()
        }

    }

    fun rollDice() {
        //check if there are any selected dice
        if (dieSet.getSelected().contains(true)) {
            rollOtherDice();
            return;
        }
        dieSet.rollDice();
        _dice.value = dieSet.getDiceSet;

    }

    fun toggleSelected(index: Int) {
        dieSet.toggleSelected(index);
        _dice.value = dieSet.getDiceSet;
    }

    fun resetSelection() {
        dieSet.resetSelection();
        _dice.value = dieSet.getDiceSet;
    }

    fun rollSelectedDice() {
        dieSet.rollSelectedDice();
        _dice.value = dieSet.getDiceSet;
    }

    fun rollOtherDice() {
        dieSet.rollOtherDice();
        _dice.value = dieSet.getDiceSet;
    }
}