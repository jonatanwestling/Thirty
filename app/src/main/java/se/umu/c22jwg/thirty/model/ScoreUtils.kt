package se.umu.c22jwg.thirty.model

fun getCombinationScore(dice: List<Int>, target: Int): Int {
    // Generate all valid combinations (subsets that sum to target)
    val allCombos = generateAllCombinations(dice, target)
    return maxScoreFromCombinations(dice, allCombos, target)
}

private fun generateAllCombinations(dice: List<Int>, target: Int): List<List<Int>> {
    val result = mutableListOf<List<Int>>()
    val sortedDice = dice.sorted()  // Optional, for consistent traversal

    fun backtrack(start: Int, path: MutableList<Int>, sum: Int) {
        if (sum == target) {
            result.add(path.toList())
            return
        }
        if (sum > target) return

        for (i in start until sortedDice.size) {
            if (i > start && sortedDice[i] == sortedDice[i - 1]) continue  // Skip duplicates
            path.add(sortedDice[i])
            backtrack(i + 1, path, sum + sortedDice[i])
            path.removeAt(path.lastIndex)
        }
    }

    backtrack(0, mutableListOf(), 0)
    return result
}

private fun maxScoreFromCombinations(
    dice: List<Int>,
    combinations: List<List<Int>>,
    target: Int
): Int {
    var maxScore = -1

    fun backtrack(used: MutableList<Boolean>, count: Int) {
        var found = false

        for (combo in combinations) {
            val tempUsed = used.toMutableList()
            var valid = true

            for (v in combo) {
                val idx = tempUsed.withIndex().indexOfFirst { (i, used) -> !used && dice[i] == v }
                if (idx == -1) {
                    valid = false
                    break
                }
                tempUsed[idx] = true
            }

            if (valid) {
                backtrack(tempUsed, count + 1)
                found = true
            }
        }

        if (!found) {
            val totalUsed = used.count { it }
            if (totalUsed == dice.size && count > 0) {
                maxScore = maxOf(maxScore, count * target)
            }
        }
    }

    backtrack(MutableList(dice.size) { false }, 0)
    return maxScore
}