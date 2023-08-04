package dev.bytebooster.chatmagicai.ai.textgen

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.exp


fun JSONObject.toMap(): Map<String, *> = keys().asSequence().associateWith {
    when (val value = this[it])
    {
        is JSONArray ->
        {
            val map = (0 until value.length()).associate { Pair(it.toString(), value[it]) }
            JSONObject(map).toMap().values.toList()
        }
        is JSONObject -> value.toMap()
        JSONObject.NULL -> null
        else            -> value
    }
}


fun JSONArray.toArrayList(): ArrayList<String> {
    val list = arrayListOf<String>()
    for (i in 0 until this.length()) {
        list.add(this.getString(i))
    }

    return list
}

fun FloatArray.argmax(): Int {
    var bestIndex = 0
    repeat(size) {
        if (this[it] > this[bestIndex]) {
            bestIndex = it
        }
    }

    return bestIndex
}


fun String.stripNonASCII(): String {
    return this.replace("[^\\x00-\\x7F]".toRegex(), "")
}


fun FloatArray.softmax(): FloatArray {
    val exponents = FloatArray(this.size)
    var sumExponents = 0f
    for (i in this.indices) {
        exponents[i] = exp(this[i])
        sumExponents += exponents[i]
    }
    for (i in exponents.indices) {
        exponents[i] = exponents[i] / sumExponents
    }
    return exponents
}


fun FloatArray.cumsum(): FloatArray {
    for (i in 1 until this.size) {
        this[i] += this[i - 1]
    }
    return this
}

fun BooleanArray.scatter(sortedIndices: FloatArray, sortedIndicesToRemove: BooleanArray): BooleanArray {
    val indicesToRemove = BooleanArray(sortedIndices.size) { false }
    for (i in sortedIndices.indices) {
        if (sortedIndicesToRemove[i]) {
            indicesToRemove[sortedIndices[i].toInt()] = true
        }
    }
    return indicesToRemove
}


fun FloatArray.multinomial(sampleSize: Int): IntArray {
    val sum = this.sum()
    val probabilities = this.map { it / sum }.toTypedArray().toFloatArray()
    val cumProbabilities = probabilities.cumsum()

    return IntArray(sampleSize) {
        val sample = Math.random()
        var index = cumProbabilities.binarySearch(sample.toFloat())
        if (index < 0) {
            index = -index - 1
        }
        index
    }
}

fun FloatArray.gather(dim: Int, indices: IntArray): FloatArray {
    return indices.map { this[it] }.toFloatArray()
}


fun FloatArray.applyTopP(topP: Float, filterValue: Float): FloatArray {
    val sortedLogits = this.filter { it != Float.NEGATIVE_INFINITY }.toFloatArray().sortedArray()
    val cumulativeProbs = sortedLogits.softmax().cumsum()

    val indicesToRemove = cumulativeProbs.map { prob ->
        prob <= (1 - topP)
    }

    val originalIndices = indicesToRemove.mapIndexed { index, remove ->
        if (remove) this.indexOfFirst { it == sortedLogits[index] } else -1
    }.filter { it != -1 }.toIntArray()

    return this.mapIndexed { index, score ->
        if (originalIndices.contains(index)) filterValue else score
    }.toFloatArray()
}
