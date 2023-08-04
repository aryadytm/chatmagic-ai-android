package dev.bytebooster.chatmagicai.ai.textgen

import ai.onnxruntime.OrtEnvironment
import android.content.Context
import org.json.JSONObject
import java.io.File


class TextGenLoader(
    private val context: Context
) {

    private val configFileName = "modelutils-min"

    fun loadTextGenModel(modelFile: File, modelType: String): TextGenerator {
        // Load Model
        val ortEnv = OrtEnvironment.getEnvironment()
        val ortSession = ortEnv.createSession(modelFile.absolutePath)

        // Load Configs
        val configName = configFileName
        val configJsonString = context.assets.open(configName).bufferedReader().use{ it.readText() }
        val tokenizer = loadTokenizer(configJsonString)
        val config = loadConfig(configJsonString, modelType)

        return TextGenerator(ortSession, ortEnv, tokenizer, config)
    }

    fun loadConfig(configJsonString: String, modelType: String): TextGeneratorConfig {
        val configJson = JSONObject(configJsonString).toMap()
        val modelConfig = configJson[modelType] as Map<String, *>
        val config = TextGeneratorConfig(
            numAttentionHeads = (modelConfig["han"] as Int).toLong() / 2,
            numHiddenSize = (modelConfig["shn"] as Int).toLong() / 2,
            numHiddenLayers = (modelConfig["lhn"] as Int).toLong() / 2,
        )
        return config
    }

    fun loadTokenizer(configJsonString: String): Tokenizer {
        val tokenizerJson = JSONObject(configJsonString)
        val encoder = tokenizerJson.getJSONObject("kot").getJSONObject("model").getJSONObject("vocab").toMap() as Map<String, Int>
        val decoder = encoder.entries.associate{ (k,v)-> v to k} as Map<Int, String>
        val mergesList: List<String> = tokenizerJson.getJSONObject("kot").getJSONObject("model").getJSONArray("merges").toArrayList()
        val bpeRank = hashMapOf<Pair<String, String>, Int>().apply {
            mergesList.forEachIndexed { i, s ->
                val list = s.split(" ")
                val keyTuple = list[0] to list[1]
                put(keyTuple, i)
            }
        }
        val tokenizer = Tokenizer(encoder, decoder, bpeRank)
        return tokenizer
    }

}