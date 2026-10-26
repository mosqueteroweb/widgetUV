package com.hermes.widgetuv

import com.hermes.widgetuv.data.api.OpenMeteoClient
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenMeteoValidationTest {
    @Test fun impossibleUvValuesBecomeZero() {
        listOf(-1.0, 26.0, Double.NaN, Double.POSITIVE_INFINITY, null).forEach {
            assertEquals(0.0, OpenMeteoClient.validUv(it), 0.0)
        }
    }

    @Test fun realUvValuesAreKept() {
        assertEquals(0.0, OpenMeteoClient.validUv(0.0), 0.0)
        assertEquals(7.25, OpenMeteoClient.validUv(7.25), 0.0)
        assertEquals(14.0, OpenMeteoClient.validUv(14.0), 0.0)
    }
}
