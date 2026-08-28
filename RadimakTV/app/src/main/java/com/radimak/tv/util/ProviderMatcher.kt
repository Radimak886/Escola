package com.radimak.tv.util

import com.radimak.tv.model.StreamingService
import java.text.Normalizer
import java.util.Locale

object ProviderMatcher {
    fun matchingService(providerName: String, services: List<StreamingService>): StreamingService? {
        val normalizedProvider = normalize(providerName)
        return services
            .asSequence()
            .filter { it.enabled }
            .firstOrNull { service ->
                service.matchTerms
                    .asSequence()
                    .map(::normalize)
                    .any { term -> normalizedProvider.contains(term) || term.contains(normalizedProvider) }
            }
    }

    fun isIncluded(providerName: String, services: List<StreamingService>): Boolean {
        return matchingService(providerName, services) != null
    }

    internal fun normalize(value: String): String = Normalizer
        .normalize(value, Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .lowercase(Locale.ROOT)
        .replace("+", " plus ")
        .replace("[^a-z0-9]+".toRegex(), " ")
        .trim()
}
