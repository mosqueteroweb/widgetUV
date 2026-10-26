package com.hermes.widgetuv.data.model

import java.util.Locale

private val spanish = Locale("es", "ES")

/** "3,5" in Spanish and "3.5" in English, as each language writes decimals. */
fun formatUv(value: Double, isEnglish: Boolean): String =
    String.format(if (isEnglish) Locale.US else spanish, "%.1f", value)
