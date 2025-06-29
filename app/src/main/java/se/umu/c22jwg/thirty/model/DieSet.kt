package se.umu.c22jwg.thirty.model
import se.umu.c22jwg.thirty.model.Die
import android.os.Parcel
import android.os.Parcelable

class DieSet() : Parcelable {
    // List of all the dice in the set
    private val diceSet: MutableList<Die> = MutableList(6) { Die() }

    // Get the list of dice in the set
    val getDiceSet: List<Die>
        get() = diceSet

    // Roll all the dice in the set
    fun rollDice(){
        for (die in diceSet){
            die.roll();
        }
    }

    // Roll the dice that are not selected in the set
    fun rollOtherDice() {
        for (die in diceSet) {
            if (!die.selected) {
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

    constructor(parcel: Parcel) : this() {
        val dice = mutableListOf<Die>()
        parcel.readTypedList(dice, Die.CREATOR)
        for (i in dice.indices) {
            diceSet[i] = dice[i]
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(diceSet)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<DieSet> {
        override fun createFromParcel(parcel: Parcel): DieSet = DieSet(parcel)
        override fun newArray(size: Int): Array<DieSet?> = arrayOfNulls(size)
    }
}