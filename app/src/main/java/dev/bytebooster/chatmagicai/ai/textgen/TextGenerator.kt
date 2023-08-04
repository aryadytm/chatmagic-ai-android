package dev.bytebooster.chatmagicai.ai.textgen

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import dev.bytebooster.chatmagicai.logDebug
import java.nio.FloatBuffer
import java.nio.IntBuffer


class TextGeneratorConfig(
    val numAttentionHeads: Long,
    val numHiddenSize: Long,
    val numHiddenLayers: Long
)

class TextGenerationRawInput(
    var inputIds: IntArray,
    var past: MutableList<OnnxTensor>,
)


class TextGenerationOutput(
    val nextTokenScores: FloatArray,
    val past: MutableList<OnnxTensor>
)


class TextGenerator(
    private val model: OrtSession,
    private val env: OrtEnvironment,
    val tokenizer: Tokenizer,
    private val config: TextGeneratorConfig
) {

    private val hardMaxTokens = 2000
    private val badWords = listOf(
        "sex", "pussy", "boob", "breast", "chest", "vagina", "porn", "dick", "cum", "oppai", "anal", "blowjob",
        "penis", "erect", "ejaculat", "orgasm", "clit", "masturbat", "horny", "impregnate", "nipple", "fuck",
        "tits", "murder", "kill", "bomb", "dynamite", "poison", "gun", "firearm", "weed", "cocaine", "opium",
        "rape", "theft", "kidnap", "terror", "explosiv", "bullet", "suicid"
    )
    private val badWordTokens = tokenizer.getBadWordTokens(badWords)

    fun generate(
        promptText: String,
        doSample: Boolean,
        temperature: Float,
        topK: Int,
        topP: Float,
        repetitionPenalty: Float,
        maxNewTokens: Int,
        onToken: (String) -> Boolean,
    ) {

        env.use {
            val proc = LogitsProcessor(
                temperature=temperature,
                topK=topK,
                topP=topP,
                repetitionPenalty=repetitionPenalty
            )

            val inputs = getFirstInput(promptText)

            logDebug(tokenizer.encode(promptText).toString())

            var allTokenIds = inputs.inputIds.clone()
            var allText = ""

            for (step in 0 until maxNewTokens) {
                val ortInputs = mutableMapOf<String, OnnxTensor>()
                ortInputs["input_ids"] = getInputIdsTensor(inputs.inputIds)

                for (i in 0 until inputs.past.size) {
                    ortInputs["past_${i}"] = inputs.past[i]
                }

                val ortOutputs = getOrtOutput(ortInputs)

                var nextTokenScores = ortOutputs.nextTokenScores

                var nextToken = 0

                if (doSample) {
                    // Apply Sampling
                    nextTokenScores = proc.applyBadWordFilter(nextTokenScores, badWordTokens)
                    nextTokenScores = proc.applyRepetitionPenalty(allTokenIds, nextTokenScores)
                    nextTokenScores = proc.applyTemperature(nextTokenScores)
                    nextTokenScores = proc.applyTopK(nextTokenScores)
                    nextTokenScores = proc.applyTopP(nextTokenScores)
                    // Get Next Token
                    val probs = nextTokenScores.softmax()
                    nextToken = probs.multinomial(1)[0]
                }
                else {
                    nextToken = nextTokenScores.argmax()
                }

                val nextTokenText = tokenizer.decode(listOf(nextToken))

                // Update input ids and past
                inputs.inputIds = intArrayOf(nextToken)
                inputs.past = ortOutputs.past

                allTokenIds += nextToken
                allText += nextTokenText

                var shouldFinish = false

                if (!onToken(nextTokenText) || nextToken == 0 || step + 2 >= maxNewTokens) {
                    shouldFinish = true
                }

                if (shouldFinish) {
                    // Close unused tensors
                    for (oldPast in inputs.past) {
                        oldPast.close()
                    }
                    break
                }
            }
        }
    }

    fun unload() {
        model.close()
        env.close()
    }

    private fun getOrtOutput(ortInputs: Map<String, OnnxTensor>): TextGenerationOutput {
        val t0 = System.currentTimeMillis()
        val output = model.run(ortInputs)

        val rawLogits = ((output.get(0).value) as Array<Array<FloatArray>>)[0]
        val nextTokenScores = rawLogits[rawLogits.size - 1]

        val past = mutableListOf<OnnxTensor>()

        for (i in 0 until config.numHiddenLayers.toInt()) {
            past.add(output.get(i+1) as OnnxTensor)
        }

        // Free unused tensors
        output.get(0).close()

        for (oldInput in ortInputs.values) {
            oldInput.close()
        }

        return TextGenerationOutput(nextTokenScores, past)
    }

    private fun getFirstInput(promptText: String): TextGenerationRawInput {
        val inputIds = tokenizer.encode(promptText)

        val emptyPast: MutableList<OnnxTensor> = mutableListOf()
        val batchSize: Long = 1
        val sequenceLength = inputIds.size
        val pastShape = longArrayOf(
            2, batchSize, config.numAttentionHeads, 1, config.numHiddenSize / config.numAttentionHeads)

        for (i in 0 until config.numHiddenLayers) {
            emptyPast.add(getEmptyTensor(pastShape))
        }
        return TextGenerationRawInput(inputIds.toIntArray(), emptyPast)
    }

    private fun getEmptyTensor(shape: LongArray): OnnxTensor {
        if (shape.size != 5) {
            TODO("Size other than 5 is not implemented!")
        }

        val tensorLength = (shape[0] * shape[1] * shape[2] * shape[3] * shape[4]).toInt()
        val emptyBuffer = FloatBuffer.allocate(tensorLength)

        for (i in 0 until tensorLength) {
            emptyBuffer.put(0.0f)
        }
        emptyBuffer.rewind()
        return OnnxTensor.createTensor(env, emptyBuffer, shape);
    }

    private fun getInputIdsTensor(inputIdsArray: IntArray): OnnxTensor {

        // Apply hard max number of tokens
        val selectedInputIdsArray: IntArray = if (inputIdsArray.size > hardMaxTokens) {
            inputIdsArray.toList().subList(inputIdsArray.size - hardMaxTokens, inputIdsArray.size).toIntArray()
        } else {
            inputIdsArray
        }

        val inBuffer = IntBuffer.allocate(selectedInputIdsArray.size)
        selectedInputIdsArray.forEach {
            inBuffer.put(it)
        }
        inBuffer.rewind()
        return OnnxTensor.createTensor(
            env,
            inBuffer,
            longArrayOf(1, selectedInputIdsArray.size.toLong())
        )
    }

}