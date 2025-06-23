package se.umu.c22jwg.thirty.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import se.umu.c22jwg.thirty.model.Die
import se.umu.c22jwg.thirty.model.DieSet

class GameViewModel: ViewModel() {
    private val dieSet = DieSet();
    private var round = 1;
    private val _dice = MutableLiveData(dieSet.getDiceSet)
    val dice: LiveData<List<Die>> = _dice

    fun rollDice() {
        //check if there are any selected dice
        if (dieSet.getSelected().contains(true)) {
            rollSelectedDice();
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
}