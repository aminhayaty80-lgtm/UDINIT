package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyHelper {

    private val persianDecimalFormat: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = '٬'
        }
        DecimalFormat("#,###", symbols)
    }

    /**
     * Formats an amount in Tomans with Persian digits and 3-digit comma separation.
     * e.g. 108000000 -> "۱۰۸٬۰۰۰٬۰۰۰ تومان"
     */
    fun formatToman(amount: Long, includeUnit: Boolean = true): String {
        if (amount == 0L) return if (includeUnit) "۰ تومان" else "۰"
        val formatted = persianDecimalFormat.format(amount)
        val persian = PersianCalendarHelper.toPersianDigits(formatted)
        return if (includeUnit) "$persian تومان" else persian
    }

    /**
     * Parses user input string (handling Persian digits, dots, commas, spaces)
     * e.g. "108.000.000", "۱۰۸,۰۰۰,۰۰۰", "108000000 تومان" -> 108000000L
     */
    fun parseAmount(input: String): Long {
        if (input.isBlank()) return 0L
        val normalized = StringBuilder()
        for (ch in input) {
            when (ch) {
                in '0'..'9' -> normalized.append(ch)
                '۰' -> normalized.append('0')
                '۱' -> normalized.append('1')
                '۲' -> normalized.append('2')
                '۳' -> normalized.append('3')
                '۴' -> normalized.append('4')
                '۵' -> normalized.append('5')
                '۶' -> normalized.append('6')
                '۷' -> normalized.append('7')
                '۸' -> normalized.append('8')
                '۹' -> normalized.append('9')
                // Ignore commas, dots, spaces, letters
                else -> {}
            }
        }
        return normalized.toString().toLongOrNull() ?: 0L
    }

    /**
     * Converts an amount into approximate human-readable Persian words
     * e.g. 108,000,000 -> "۱۰۸ میلیون تومان"
     */
    fun toFriendlyTomanWords(amount: Long): String {
        if (amount <= 0) return "۰ تومان"
        val billion = 1_000_000_000L
        val million = 1_000_000L
        val thousand = 1_000L

        return when {
            amount >= billion -> {
                val b = amount / billion
                val remM = (amount % billion) / million
                if (remM > 0) {
                    "${PersianCalendarHelper.toPersianDigits(b.toString())} میلیارد و ${PersianCalendarHelper.toPersianDigits(remM.toString())} میلیون تومان"
                } else {
                    "${PersianCalendarHelper.toPersianDigits(b.toString())} میلیارد تومان"
                }
            }
            amount >= million -> {
                val m = amount / million
                val remK = (amount % million) / thousand
                if (remK > 0) {
                    "${PersianCalendarHelper.toPersianDigits(m.toString())} میلیون و ${PersianCalendarHelper.toPersianDigits(remK.toString())} هزار تومان"
                } else {
                    "${PersianCalendarHelper.toPersianDigits(m.toString())} میلیون تومان"
                }
            }
            amount >= thousand -> {
                val k = amount / thousand
                "${PersianCalendarHelper.toPersianDigits(k.toString())} هزار تومان"
            }
            else -> {
                "${PersianCalendarHelper.toPersianDigits(amount.toString())} تومان"
            }
        }
    }
}
