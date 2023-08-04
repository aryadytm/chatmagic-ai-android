package dev.bytebooster.chatmagicai.ai.textgen

import java.util.*


class LogitsProcessor(
    val temperature: Float,
    val topK: Int,
    val topP: Float,
    val repetitionPenalty: Float
    ) {

    private val filterValue: Float = Float.NEGATIVE_INFINITY
    private val minTokensToKeep: Int = 1

    fun applyBadWordFilter(scores: FloatArray, badWordTokens: List<Int>): FloatArray {
        for (token in badWordTokens) {
            scores[token] = filterValue
        }
        return scores
    }

    fun applyTemperature(scores: FloatArray): FloatArray {
        for (i in scores.indices) {
            scores[i] = scores[i] / temperature
        }
        return scores
    }

    fun applyTopP(scores: FloatArray): FloatArray {

        val filteredLogitsWithIndexes = scores
            .mapIndexed { index, fl -> (index to fl) }
            .filter { it.second > Float.NEGATIVE_INFINITY }
            .sortedBy { it.second }

        val cumProbs = filteredLogitsWithIndexes.map { it.second }
            .toFloatArray().softmax().cumsum()


        val topPFiltered = cumProbs.map {
            if (it <= (1 - topP)) filterValue else it
        }

        val indicesToKeep = mutableListOf<Int>()

        topPFiltered.mapIndexed { index, fl->
            if (fl > 0) {
                indicesToKeep.add(filteredLogitsWithIndexes[index].first)
            }
        }

        for (i in scores.indices) {
            if (i !in indicesToKeep) scores[i] = this.filterValue
        }

        return scores
    }

    fun applyTopK(scores: FloatArray): FloatArray {

        val average = scores.average()

        var filteredLogitsWithIndexes = scores
            .mapIndexed { index, fl -> (index to fl) }
            .filter { it.second > average }

        val average2 = filteredLogitsWithIndexes.map { it.second }.average()
        filteredLogitsWithIndexes = filteredLogitsWithIndexes.filter { it.second > average2 }

        val average3 = filteredLogitsWithIndexes.map { it.second }.average()
        filteredLogitsWithIndexes = filteredLogitsWithIndexes.filter { it.second > average3 }

        val average4 = filteredLogitsWithIndexes.map { it.second }.average()
        filteredLogitsWithIndexes = filteredLogitsWithIndexes.filter { it.second > average4 }

        val average5 = filteredLogitsWithIndexes.map { it.second }.average()
        filteredLogitsWithIndexes = filteredLogitsWithIndexes
            .filter { it.second > average5 }
            .sortedByDescending { it.second }
            .take(topK)

        val indices = filteredLogitsWithIndexes.map { it.first }

        for (i in scores.indices) {
            if (i !in indices) scores[i] = this.filterValue
        }
        return scores
    }

    fun applyRepetitionPenalty(inputIds: IntArray, scores: FloatArray): FloatArray {
        if (this.repetitionPenalty == 1.0f) {
            return scores
        }
        val score = inputIds.map { scores[it] }.toFloatArray()
        // if score < 0 then repetition penalty has to be multiplied to reduce
        // the previous token probability
        val penaltyScores = score.map {
            if (it < 0) it * this.repetitionPenalty else it / this.repetitionPenalty
        }

        inputIds.forEachIndexed { index, token -> scores[token] = penaltyScores[index] }

        return scores
    }

}


// Radix sort Java implementation

internal object Radix {
    // A utility function to get maximum value in arr[]
    fun getMax(arr: IntArray, n: Int): Int {
        var mx = arr[0]
        for (i in 1 until n) if (arr[i] > mx) mx = arr[i]
        return mx
    }

    // A function to do counting sort of arr[] according to
    // the digit represented by exp.
    fun countSort(arr: IntArray, n: Int, exp: Int) {
        val output = IntArray(n) // output array
        var i: Int
        val count = IntArray(10)
        Arrays.fill(count, 0)

        // Store count of occurrences in count[]
        i = 0
        while (i < n) {
            count[arr[i] / exp % 10]++
            i++
        }

        // Change count[i] so that count[i] now contains
        // actual position of this digit in output[]
        i = 1
        while (i < 10) {
            count[i] += count[i - 1]
            i++
        }

        // Build the output array
        i = n - 1
        while (i >= 0) {
            output[count[arr[i] / exp % 10] - 1] = arr[i]
            count[arr[i] / exp % 10]--
            i--
        }

        // Copy the output array to arr[], so that arr[] now
        // contains sorted numbers according to current
        // digit
        i = 0
        while (i < n) {
            arr[i] = output[i]
            i++
        }
    }

    // The main function to that sorts arr[] of
    // size n using Radix Sort
    fun radixsort(arr: IntArray, n: Int) {
        // Find the maximum number to know number of digits
        val m = getMax(arr, n)

        // Do counting sort for every digit. Note that
        // instead of passing digit number, exp is passed.
        // exp is 10^i where i is current digit number
        var exp = 1
        while (m / exp > 0) {
            countSort(arr, n, exp)
            exp *= 10
        }
    }

    // A utility function to print an array
    fun print(arr: IntArray, n: Int) {
        for (i in 0 until n) print(arr[i].toString() + " ")
    }

    // Main driver method
    @JvmStatic
    fun main(args: Array<String>) {
        val arr = intArrayOf(170, 45, 75, 90, 802, 24, 2, 66)
        val n = arr.size

        // Function Call
        radixsort(arr, n)
        print(arr, n)
    }
}