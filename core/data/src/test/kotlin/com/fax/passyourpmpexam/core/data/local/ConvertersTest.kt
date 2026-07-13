package com.fax.passyourpmpexam.core.data.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun optionsRoundTripThroughJson() {
        val options = listOf("Initiating", "Planning", "Executing", "Closing")
        val encoded = converters.fromStringList(options)
        assertEquals(options, converters.toStringList(encoded))
    }

    @Test
    fun emptyListRoundTrips() {
        assertEquals(emptyList(), converters.toStringList(converters.fromStringList(emptyList())))
    }

    @Test
    fun preservesValuesContainingCommasAndQuotes() {
        val tricky = listOf("A, then B", "\"quoted\"", "back\\slash")
        assertEquals(tricky, converters.toStringList(converters.fromStringList(tricky)))
    }

    @Test
    fun encodesAsJsonArray() {
        assertTrue(converters.fromStringList(listOf("x")).startsWith("["))
    }
}
