package com.bintianqi.owndroid.feature.focus

import kotlinx.serialization.json.Json
import java.io.File

class FocusRepository(private val file: File) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    var config: FocusConfig = load()
        private set

    private fun load(): FocusConfig {
        return if (file.exists()) {
            try {
                json.decodeFromString<FocusConfig>(file.readText())
            } catch (e: Exception) {
                FocusConfig()
            }
        } else {
            FocusConfig()
        }
    }

    fun update(block: (FocusConfig) -> FocusConfig) {
        config = block(config)
        try {
            file.writeText(json.encodeToString(config))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
