package com.hermes.widgetuv.worker

import com.hermes.widgetuv.data.model.UvRiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class UvScheduleManagerTest {

    @Test
    fun testDelayBeforeTargetTime() {
        // Given current time is 06:15 on May 10, 2026
        val from = LocalDateTime.of(2026, 5, 10, 6, 15, 0)
        // Target is 07:00
        val delay = UvScheduleManager.calculateDelayToNext(7, 0, from)
        assertEquals(Duration.ofMinutes(45), delay)
    }

    @Test
    fun testDelayAfterTargetTimeNextDayRollover() {
        // Given current time is 15:30 on May 10, 2026
        val from = LocalDateTime.of(2026, 5, 10, 15, 30, 0)
        // Target is 14:00 (has already passed today, rolls over to tomorrow 14:00)
        val delay = UvScheduleManager.calculateDelayToNext(14, 0, from)
        // Remaining time today: 8h 30m + tomorrow until 14:00: 14h = 22h 30m
        assertEquals(Duration.ofHours(22).plusMinutes(30), delay)
    }

    @Test
    fun testLeapYearRollover() {
        // Given February 28 on a leap year (2024 is a leap year) at 23:00
        val fromLeap = LocalDateTime.of(2024, 2, 28, 23, 0, 0)
        val delay = UvScheduleManager.calculateDelayToNext(7, 0, fromLeap)
        // Delay is 8 hours
        assertEquals(Duration.ofHours(8), delay)
    }

    @Test
    fun testUvRiskLevelsThresholds() {
        assertEquals(UvRiskLevel.LOW, UvRiskLevel.fromValue(0.0))
        assertEquals(UvRiskLevel.LOW, UvRiskLevel.fromValue(2.9))
        assertEquals(UvRiskLevel.MODERATE, UvRiskLevel.fromValue(3.0))
        assertEquals(UvRiskLevel.MODERATE, UvRiskLevel.fromValue(5.9))
        assertEquals(UvRiskLevel.HIGH, UvRiskLevel.fromValue(6.0))
        assertEquals(UvRiskLevel.HIGH, UvRiskLevel.fromValue(7.9))
        assertEquals(UvRiskLevel.VERY_HIGH, UvRiskLevel.fromValue(8.0))
        assertEquals(UvRiskLevel.VERY_HIGH, UvRiskLevel.fromValue(10.9))
        assertEquals(UvRiskLevel.EXTREME, UvRiskLevel.fromValue(11.0))
        assertEquals(UvRiskLevel.EXTREME, UvRiskLevel.fromValue(15.5))
    }
}
