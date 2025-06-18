package se.umu.c22jwg.thirty.model
import se.umu.c22jwg.thirty.model.Die

class DieSet {
    // List of all the dice in the set
    private val diceSet: List<Die> = List(6) { Die() }

    // Get the list of dice in the set
    val getDiceSet: List<Die>
        get() = diceSet

    // Get the current values of the dice set
    fun getValues(): List<Int> {
        return diceSet.map { it.value }
    }

    // Roll all the dice in the set
    fun rollDice(){
        for (die in diceSet){
            die.roll();
        }
    }
    // Roll the selected dice in the set
    fun rollSelectedDice() {
        for (die in diceSet) {
            if (die.selected) {
                die.roll();
            }
        }
    }

    // Toggle selection of a die at a given index
    fun toggleSelected(index: Int) {
        if (index in diceSet.indices) {
            diceSet[index].selected = !diceSet[index].selected
        }
    }

    // Get list of which dice are selected
    fun getSelected(): List<Boolean> {
        return diceSet.map { it.selected }
    }

    // Reset all selections to false
    fun resetSelection() {
        diceSet.forEach { it.selected = false }
    }
}