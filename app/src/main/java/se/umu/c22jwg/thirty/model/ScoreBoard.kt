package se.umu.c22jwg.thirty.model

import android.os.Parcel
import android.os.Parcelable
class ScoreBoard() : Parcelable {
    // List to store scores
    private val scores: MutableList<Pair<String, Int>> = mutableListOf()

    constructor(parcel: Parcel) : this() {
        val size = parcel.readInt()
        repeat(size) {
            val choice = parcel.readString() ?: ""
            val score = parcel.readInt()
            scores.add(choice to score)
        }
    }

    fun addScore(choice: String, score: Int) {
        scores.add(choice to score)
    }

    fun getScores(): List<Pair<String, Int>> = scores.toList()

    fun getScoreForRound(round: Int): Pair<String, Int> = scores[round]

    fun calculateTotalScore(): Int = scores.sumOf { it.second }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(scores.size)
        for ((choice, score) in scores) {
            parcel.writeString(choice)
            parcel.writeInt(score)
        }
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ScoreBoard> {
        override fun createFromParcel(parcel: Parcel): ScoreBoard = ScoreBoard(parcel)
        override fun newArray(size: Int): Array<ScoreBoard?> = arrayOfNulls(size)
    }
}