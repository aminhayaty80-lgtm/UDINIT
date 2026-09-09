package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Utility for converting Gregorian dates to Persian (Solar Hijri / Jalali)
 * and formatting date-times in Persian.
 */
object PersianCalendarHelper {

    data class PersianDate(
        val year: Int,
        val month: Int,
        val day: Int
    )

    private val PERSIAN_MONTH_NAMES = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    private val PERSIAN_WEEKDAYS = arrayOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه", "شنبه"
    )

    /**
     * Converts Gregorian Year, Month (1-12), Day to PersianDate
     */
    fun gregorianToPersian(gYear: Int, gMonth: Int, gDay: Int): PersianDate {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy = gYear - 1600
        var gm = gMonth - 1
        var gd = gDay - 1

        var gDayNo = 365 * gy + ((gy + 3) / 4) - ((gy + 99) / 100) + ((gy + 399) / 400)

        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        var jd = 0
        for (i in 0 until 11) {
            if (jDayNo < jDaysInMonth[i]) {
                jm = i
                jd = jDayNo + 1
                break
            }
            jDayNo -= jDaysInMonth[i]
            jm = 11
            jd = jDayNo + 1
        }

        return PersianDate(jy, jm + 1, jd)
    }

    /**
     * Returns a human readable Persian formatted date and time for a timestamp
     * e.g. "سه‌شنبه ۱۸ شهریور ۱۴۰۵ - ساعت ۱۴:۳۰"
     */
    fun formatToPersianDateTime(timestampMillis: Long): String {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestampMillis
        }

        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1
        val gDay = cal.get(Calendar.DAY_OF_MONTH)
        val pDate = gregorianToPersian(gYear, gMonth, gDay)

        val dayOfWeekIdx = cal.get(Calendar.DAY_OF_WEEK) - 1
        val dayName = PERSIAN_WEEKDAYS[dayOfWeekIdx.coerceIn(0, 6)]
        val monthName = PERSIAN_MONTH_NAMES[(pDate.month - 1).coerceIn(0, 11)]

        val hour = String.format(Locale.US, "%02d", cal.get(Calendar.HOUR_OF_DAY))
        val minute = String.format(Locale.US, "%02d", cal.get(Calendar.MINUTE))

        val fullStr = "$dayName ${toPersianDigits(pDate.day.toString())} $monthName ${toPersianDigits(pDate.year.toString())} - ساعت ${toPersianDigits("$hour:$minute")}"
        return fullStr
    }

    /**
     * Short Persian date e.g. "۱۴۰۵/۰۶/۱۸"
     */
    fun formatToPersianDateShort(timestampMillis: Long): String {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestampMillis
        }
        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1
        val gDay = cal.get(Calendar.DAY_OF_MONTH)
        val pDate = gregorianToPersian(gYear, gMonth, gDay)

        val mStr = String.format(Locale.US, "%02d", pDate.month)
        val dStr = String.format(Locale.US, "%02d", pDate.day)
        return toPersianDigits("${pDate.year}/$mStr/$dStr")
    }

    /**
     * Returns days elapsed between the given timestamp and now.
     */
    fun getDaysElapsed(timestampMillis: Long): Long {
        val diff = System.currentTimeMillis() - timestampMillis
        return if (diff > 0) diff / (1000L * 60 * 60 * 24) else 0L
    }

    /**
     * Returns true if more than 14 days (2 weeks) have passed since timestamp
     */
    fun isOlderThan14Days(timestampMillis: Long): Boolean {
        val twoWeeksMillis = 14L * 24 * 60 * 60 * 1000L
        return (System.currentTimeMillis() - timestampMillis) >= twoWeeksMillis
    }

    /**
     * Friendly relative time in Persian
     */
    fun getRelativePersianTime(timestampMillis: Long): String {
        val days = getDaysElapsed(timestampMillis)
        return when {
            days == 0L -> "امروز"
            days == 1L -> "دیروز"
            days < 7L -> "${toPersianDigits(days.toString())} روز پیش"
            days < 14L -> "${toPersianDigits((days / 7).toString())} هفته پیش"
            days < 30L -> "${toPersianDigits((days / 7).toString())} هفته پیش (نیاز به پیگیری)"
            else -> "${toPersianDigits((days / 30).toString())} ماه پیش"
        }
    }

    /**
     * Converts English numbers (0-9) to Persian digits (۰-۹)
     */
    fun toPersianDigits(input: String): String {
        val persianChars = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(persianChars[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}
