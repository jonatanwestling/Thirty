package se.umu.c22jwg.thirty.model

import android.os.Parcel
import android.os.Parcelable

data class GameState(
    val dieSet: DieSet = DieSet(),
    val scoreBoard: ScoreBoard = ScoreBoard(),
    val currentRound: Int = 1,
    val currentRoll: Int = 0,
    val currentScore: Int = 0,
    val rollButtonEnabled: Boolean = true,
    val nextButtonEnabled: Boolean = true,
    val showFinish: Boolean = false,
    val navigateToResult: Boolean = false,
    val remainingChoices: MutableList<String> = mutableListOf("Low", "4", "5", "6", "7", "8", "9", "10", "11", "12"),
    val selectedChoice: String = "Low",
    val isDieSelectionEnabled: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        dieSet = parcel.readParcelable(DieSet::class.java.classLoader) ?: DieSet(),
        scoreBoard = parcel.readParcelable(ScoreBoard::class.java.classLoader) ?: ScoreBoard(),
        currentRound = parcel.readInt(),
        currentRoll = parcel.readInt(),
        currentScore = parcel.readInt(),
        rollButtonEnabled = parcel.readByte() != 0.toByte(),
        nextButtonEnabled = parcel.readByte() != 0.toByte(),
        showFinish = parcel.readByte() != 0.toByte(),
        navigateToResult = parcel.readByte() != 0.toByte(),
        remainingChoices = parcel.createStringArrayList()?.toMutableList() ?: mutableListOf(),
        selectedChoice = parcel.readString() ?: "Low",
        isDieSelectionEnabled = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(dieSet, flags)
        parcel.writeParcelable(scoreBoard, flags)
        parcel.writeInt(currentRound)
        parcel.writeInt(currentRoll)
        parcel.writeInt(currentScore)
        parcel.writeByte(if (rollButtonEnabled) 1 else 0)
        parcel.writeByte(if (nextButtonEnabled) 1 else 0)
        parcel.writeByte(if (showFinish) 1 else 0)
        parcel.writeByte(if (navigateToResult) 1 else 0)
        parcel.writeStringList(remainingChoices)
        parcel.writeString(selectedChoice)
        parcel.writeByte(if (isDieSelectionEnabled) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<GameState> {
        override fun createFromParcel(parcel: Parcel): GameState {
            return GameState(parcel)
        }

        override fun newArray(size: Int): Array<GameState?> {
            return arrayOfNulls(size)
        }
    }

    // Helper functions for state transitions
    fun copyWith(
        dieSet: DieSet = this.dieSet,
        scoreBoard: ScoreBoard = this.scoreBoard,
        currentRound: Int = this.currentRound,
        currentRoll: Int = this.currentRoll,
        currentScore: Int = this.currentScore,
        rollButtonEnabled: Boolean = this.rollButtonEnabled,
        nextButtonEnabled: Boolean = this.nextButtonEnabled,
        showFinish: Boolean = this.showFinish,
        navigateToResult: Boolean = this.navigateToResult,
        remainingChoices: MutableList<String> = this.remainingChoices,
        selectedChoice: String = this.selectedChoice,
        isDieSelectionEnabled: Boolean = this.isDieSelectionEnabled
    ): GameState {
        return copy(
            dieSet = dieSet,
            scoreBoard = scoreBoard,
            currentRound = currentRound,
            currentRoll = currentRoll,
            currentScore = currentScore,
            rollButtonEnabled = rollButtonEnabled,
            nextButtonEnabled = nextButtonEnabled,
            showFinish = showFinish,
            navigateToResult = navigateToResult,
            remainingChoices = remainingChoices,
            selectedChoice = selectedChoice,
            isDieSelectionEnabled = isDieSelectionEnabled
        )
    }

    fun isGameOver(): Boolean = currentRound > 10

    fun canRoll(): Boolean = currentRoll < 3 && rollButtonEnabled

    fun shouldShowFinish(): Boolean = currentRound == 10 && showFinish
} 