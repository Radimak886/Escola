package com.radimak.tv.util

import com.radimak.tv.data.UserPreferences
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderMatcherTest {
    @Test
    fun `reconhece os servicos ativos do usuario`() {
        val services = UserPreferences.defaultServices

        assertTrue(ProviderMatcher.isIncluded("Amazon Prime Video", services))
        assertTrue(ProviderMatcher.isIncluded("Disney Plus", services))
        assertTrue(ProviderMatcher.isIncluded("HBO Max", services))
        assertTrue(ProviderMatcher.isIncluded("Globoplay", services))
    }

    @Test
    fun `nao considera paramount quando esta desativado`() {
        assertFalse(ProviderMatcher.isIncluded("Paramount Plus", UserPreferences.defaultServices))
    }

    @Test
    fun `reconhece os provedores gratuitos adicionados`() {
        val services = UserPreferences.defaultServices

        assertEquals("plex", ProviderMatcher.matchingService("Plex", services)?.id)
        assertEquals("vix", ProviderMatcher.matchingService("ViX Gratis", services)?.id)
        assertEquals("pluto", ProviderMatcher.matchingService("Pluto TV", services)?.id)
    }
}
