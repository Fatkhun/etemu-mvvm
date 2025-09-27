package com.fatkhun.core.utils

import android.text.format.DateUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.EnumSet
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object FormatDateTime {

    const val FORMAT_DATE_DMY = "dd-MM-yyyy"
    const val FORMAT_DATE_DMYHM = "dd-MM-yyyy HH:mm"
    const val FORMAT_DATE_DMYHMS = "dd-MM-yyyy HH:mm:ss"
    const val FORMAT_DATE_DMY_LONG_MONTH = "dd-MMMM-yyyy"
    const val FORMAT_DATE_DMY_LONG_MONTH_NO_SEPARATOR = "dd MMMM yyyy"
    const val FORMAT_DATE_DMY_SLASH = "dd/MM/yyyy"
    const val FORMAT_DATE_EDMY_LONG_MONTH = "EEEE, dd MMMM yyyy"
    const val FORMAT_DATE_EDMY_LONG_DAY = "MMMM dd, yyyy"
    const val FORMAT_DATE_EDMYHM_LONG_MONTH = "EEEE, dd MMMM yyyy HH:mm"
    const val FORMAT_DATE_EDMYHM_LONG_MONTH_WITH_DOT = "EEEE, dd MMMM yyyy &#8226; HH:mm"
    const val FORMAT_DATE_EDMYHMS_LONG_MONTH = "EEEE, dd MMMM yyyy HH:mm:ss"
    const val FORMAT_DATE_EHM_LONG_DAY = "EEEE, HH:mm"
    const val FORMAT_DATE_EHM_LONG_DAY_DOT_TIME = "EEEE, HH.mm"
    const val FORMAT_DATE_YMD = "yyyy-MM-dd"
    const val FORMAT_DATE_DAY = "dd"
    const val FORMAT_DATE_MONTH = "MM"
    const val FORMAT_DATE_MONTH_LONG = "MMMM"
    const val FORMAT_DATE_YEAR = "yyyy"
    const val FORMAT_DATE_YM_LONG = "MMMM yyyy"
    const val FORMAT_DATE_YMD_NO_SEPARATOR = "yyyyMMdd"
    const val FORMAT_DATE_MDY_SLASH = "MM/dd/yyyy"
    const val FORMAT_DATE_YMD_SLASH = "yyyy/MM/dd"
    const val FORMAT_DATE_TIME_YMDHMS_SLASH = "yyyy/MM/dd HH:mm:ss"
    const val FORMAT_DATE_TIME_YMDHM = "yyyy-MM-dd HH:mm"
    const val FORMAT_DATE_TIME_YMDHMS = "yyyy-MM-dd HH:mm:ss"
    const val FORMAT_DATE_TIME_YMDTHMSZ = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    const val FORMAT_DATE_TIME_YMDHMS_NO_SEPARATOR = "yyyyMMddHHmmss"
    const val FORMAT_DATE_TIME_DMYHM_LONG_MONTH = "dd-MMMM-yyyy HH:mm"
    const val FORMAT_DATE_TIME_DMYHM_LONG_MONTH_NO_SEPARATOR = "dd MMMM yyyy HH:mm"
    const val FORMAT_DATE_TIME_DMYHM_LONG_MONTH_NO_SEPARATOR_DOT_TIME = "dd MMMM yyyy HH.mm"
    const val FORMAT_DATE_TIME_DMYHM_LONG_MONTH_COMMA_WITH_NO_SEPARATOR = "dd MMMM yyyy, HH:mm"
    const val FORMAT_DATE_TIME_DMYHM_LONG_MONTH_COMMA_WITH_NO_SEPARATOR_DOT_TIME =
        "dd MMMM yyyy, HH.mm"
    const val FORMAT_DATE_TIME_DMYHM_SHORT_MONTH_COMMA_WITH_NO_SEPARATOR_DOT_TIME =
        "dd MMM yyyy, HH.mm"
    const val FORMAT_DATE_TIME_DMYHM_SHORT_MONTH_NO_SEPARATOR = "dd MMM yyyy HH:mm"
    const val FORMAT_DATE_TIME_DMYHM_SHORT_MONTH_NO_SEPARATOR_DOT_TIME = "dd MMM yyyy HH.mm"
    const val FORMAT_DATE_TIME_DMY_LONG_MONTH_NO_SEPARATOR = "dd MMMM yyyy"
    const val FORMAT_DATE_TIME_DMY_SHORT_MONTH_NO_SEPARATOR = "dd MMM yyyy"
    const val FORMAT_DATE_DMYHMS_DASH_SEPARATOR = "dd MMMM yyyy - HH:mm:ss"
    const val FORMAT_DATE_TIME_DM_SHORT_MONTH_NO_SEPARATOR = "dd MMM"
    const val FORMAT_DATE_TIME_DMHM_SHORT_MONTH_NO_SEPARATOR = "dd MMM HH:mm"
    const val FORMAT_DATE_TIME_DMHM_SHORT_MONTH_NO_SEPARATOR_DOT_TIME = "dd MMM HH.mm"
    const val FORMAT_TIME_HM = "HH:mm"
    const val FORMAT_TIME_HM_DOT_TIME = "HH.mm"
    const val FORMAT_TIME_HMS = "HH:mm:ss"
    const val FORMAT_TIME_HH = "HH"
    const val FORMAT_TIME_MM = "mm"
    const val FORMAT_TIME_SS = "ss"
    const val FORMAT_TIME_HM_NO_SEPARATOR = "HHmm"
    const val FORMAT_DATE_TIME_GMT = "EEE MMM dd HH:mm:ss zzz yyyy"
    const val FORMAT_DATE_TIME_WIB = "EEE MMM dd HH:mm:ss z yyyy"

    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS

    fun getLastMidnight(): Date {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0
        return cal.time
    }

    fun format(date: Date, format: String): String {
        val frm = SimpleDateFormat(format, Locale.getDefault())
        return frm.format(date)
    }

    fun formatSetLocaleDefault(
        date: Date,
        format: String,
        locale: Locale = Locale.ENGLISH
    ): String {
        val frm = SimpleDateFormat(format, locale)
        return frm.format(date)
    }

    fun parse(date: String, format: String, def: Date): Date {
        val frm = SimpleDateFormat(format, Locale.getDefault())
        return try {
            frm.parse(date)
        } catch (e: ParseException) {
            def
        }
    }

    fun parseSetLocaleDefault(date: String, format: String, locale: Locale, def: Date): Date {
        val frm = SimpleDateFormat(format, locale)
        return try {
            frm.parse(date)
        } catch (e: ParseException) {
            def
        }
    }

    fun parse(date: String, format: String): Date {
        return parse(date, format, Date())
    }

    fun parseSetLocaleDefault(date: String, format: String, locale: Locale = Locale.ENGLISH): Date {
        return parseSetLocaleDefault(date, format, locale, Date())
    }

    fun parse(date: String, format_awal: String, format_akhir: String): String {
        return parse(date, format_awal, format_akhir, Date())
    }

    fun parseSetLocaleDefault(
        date: String,
        format_awal: String,
        format_akhir: String,
        locale: Locale = Locale.ENGLISH
    ): String {
        return parseSetLocaleDefault(date, format_awal, format_akhir, locale, Date())
    }

    fun parse(date: String, format_awal: String, format_akhir: String, def: Date): String {
        val _date = parse(date, format_awal, def)
        return format(_date, format_akhir)
    }

    fun parseSetLocaleDefault(
        date: String,
        format_awal: String,
        format_akhir: String,
        locale: Locale = Locale.ENGLISH,
        def: Date
    ): String {
        val _date = parseSetLocaleDefault(date, format_awal, locale, def)
        return formatSetLocaleDefault(_date, format_akhir)
    }

    fun diffDate(date_start: Date, date_end: Date): Long {
        val diffInMillies = date_end.time - date_start.time
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)
    }

    fun diffHour(date_start: Date, date_end: Date): Long {
        val diffInMillies = date_end.time - date_start.time
        return TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS)
    }

    fun diffMinutes(date_start: Date, date_end: Date): Long {
        val diffInMillies = date_end.time - date_start.time
        return TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS)
    }

    fun diffMillisecond(date_start: Date, date_end: Date): Long {
        val diffInMillies = date_end.time - date_start.time
        return TimeUnit.MILLISECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS)
    }

    fun diffHourMinutes(date_start: Date, date_end: Date): String {
        val diffInMillies = date_end.time - date_start.time
        val hours = TimeUnit.MILLISECONDS.toHours(diffInMillies)
        return String.format(
            Locale.getDefault(), "%d jam %d menit",
            hours, TimeUnit.MILLISECONDS.toMinutes(diffInMillies) - TimeUnit.HOURS.toMinutes(hours)
        )
    }

    fun millisToHourMinuteSeconds(diffInMillies: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(diffInMillies)
        val minutes =
            TimeUnit.MILLISECONDS.toMinutes(diffInMillies) - TimeUnit.HOURS.toMinutes(hours)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(diffInMillies) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(
                minutes
            )
        return String.format(
            Locale.getDefault(),
            "%d jam %d menit %d detik",
            hours,
            minutes,
            seconds
        )
    }

    /**
     *
     * Checks if two dates are on the same day ignoring time.
     *
     * @param date1 the first date, not altered, not null
     * @param date2 the second date, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either date is `null`
     */
    fun isSameDay(date1: Date?, date2: Date?): Boolean {
        require(!(date1 == null || date2 == null)) { "The dates must not be null" }
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameDay(cal1, cal2)
    }

    /**
     *
     * Checks if two calendars represent the same day ignoring time.
     *
     * @param cal1 the first calendar, not altered, not null
     * @param cal2 the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is `null`
     */
    fun isSameDay(cal1: Calendar?, cal2: Calendar?): Boolean {
        require(!(cal1 == null || cal2 == null)) { "The dates must not be null" }
        return cal1[Calendar.ERA] == cal2[Calendar.ERA] && cal1[Calendar.YEAR] == cal2[Calendar.YEAR] && cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
    }

    /**
     *
     * Checks if a date is today.
     *
     * @param date the date, not altered, not null.
     * @return true if the date is today.
     * @throws IllegalArgumentException if the date is `null`
     */
    fun isToday(date: Date?): Boolean {
        return isSameDay(date, Calendar.getInstance().time)
    }

    /**
     *
     * Checks if a calendar date is today.
     *
     * @param cal the calendar, not altered, not null
     * @return true if cal date is today
     * @throws IllegalArgumentException if the calendar is `null`
     */
    fun isToday(cal: Calendar): Boolean {
        return isSameDay(cal, Calendar.getInstance())
    }

    /**
     *
     * Checks if the first date is before the second date ignoring time.
     *
     * @param date1 the first date, not altered, not null
     * @param date2 the second date, not altered, not null
     * @return true if the first date day is before the second date day.
     * @throws IllegalArgumentException if the date is `null`
     */
    fun isBeforeDay(date1: Date?, date2: Date?): Boolean {
        require(!(date1 == null || date2 == null)) { "The dates must not be null" }
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isBeforeDay(cal1, cal2)
    }

    /**
     *
     * Checks if the first calendar date is before the second calendar date ignoring time.
     *
     * @param cal1 the first calendar, not altered, not null.
     * @param cal2 the second calendar, not altered, not null.
     * @return true if cal1 date is before cal2 date ignoring time.
     * @throws IllegalArgumentException if either of the calendars are `null`
     */
    fun isBeforeDay(cal1: Calendar?, cal2: Calendar?): Boolean {
        require(!(cal1 == null || cal2 == null)) { "The dates must not be null" }
        return cal1[Calendar.ERA] < cal2[Calendar.ERA] || cal1[Calendar.ERA] <= cal2[Calendar.ERA] && (cal1[Calendar.YEAR] < cal2[Calendar.YEAR] || cal1[Calendar.YEAR] <= cal2[Calendar.YEAR] && cal1[Calendar.DAY_OF_YEAR] < cal2[Calendar.DAY_OF_YEAR])
    }

    /**
     *
     * Checks if the first date is after the second date ignoring time.
     *
     * @param date1 the first date, not altered, not null
     * @param date2 the second date, not altered, not null
     * @return true if the first date day is after the second date day.
     * @throws IllegalArgumentException if the date is `null`
     */
    fun isAfterDay(date1: Date?, date2: Date?): Boolean {
        require(!(date1 == null || date2 == null)) { "The dates must not be null" }
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isAfterDay(cal1, cal2)
    }

    /**
     *
     * Checks if the first calendar date is after the second calendar date ignoring time.
     *
     * @param cal1 the first calendar, not altered, not null.
     * @param cal2 the second calendar, not altered, not null.
     * @return true if cal1 date is after cal2 date ignoring time.
     * @throws IllegalArgumentException if either of the calendars are `null`
     */
    fun isAfterDay(cal1: Calendar?, cal2: Calendar?): Boolean {
        require(!(cal1 == null || cal2 == null)) { "The dates must not be null" }
        return cal1[Calendar.ERA] >= cal2[Calendar.ERA] && (cal1[Calendar.ERA] > cal2[Calendar.ERA] || cal1[Calendar.YEAR] >= cal2[Calendar.YEAR] && (cal1[Calendar.YEAR] > cal2[Calendar.YEAR] || cal1[Calendar.DAY_OF_YEAR] > cal2[Calendar.DAY_OF_YEAR]))
    }

    /**
     *
     * Checks if a date is after today and within a number of days in the future.
     *
     * @param date the date to check, not altered, not null.
     * @param days the number of days.
     * @return true if the date day is after today and within days in the future .
     * @throws IllegalArgumentException if the date is `null`
     */
    fun isWithinDaysFuture(date: Date?, days: Int): Boolean {
        requireNotNull(date) { "The date must not be null" }
        val cal = Calendar.getInstance()
        cal.time = date
        return isWithinDaysFuture(cal, days)
    }

    /**
     *
     * Checks if a calendar date is after today and within a number of days in the future.
     *
     * @param cal  the calendar, not altered, not null
     * @param days the number of days.
     * @return true if the calendar date day is after today and within days in the future .
     * @throws IllegalArgumentException if the calendar is `null`
     */
    fun isWithinDaysFuture(cal: Calendar?, days: Int): Boolean {
        requireNotNull(cal) { "The date must not be null" }
        val today = Calendar.getInstance()
        val future = Calendar.getInstance()
        future.add(Calendar.DAY_OF_YEAR, days)
        return isAfterDay(cal, today) && !isAfterDay(cal, future)
    }

    /**
     * Returns the given date with the time set to the start of the day.
     */
    fun getStart(date: Date): Date? {
        return clearTime(date)
    }

    /**
     * Returns the given date with the time values cleared.
     */
    fun clearTime(date: Date?): Date? {
        if (date == null) {
            return null
        }
        val c = Calendar.getInstance()
        c.time = date
        c[Calendar.HOUR_OF_DAY] = 0
        c[Calendar.MINUTE] = 0
        c[Calendar.SECOND] = 0
        c[Calendar.MILLISECOND] = 0
        return c.time
    }

    /** Determines whether or not a date has any time values (hour, minute,
     * seconds or millisecondsReturns the given date with the time values cleared. */

    /** Determines whether or not a date has any time values (hour, minute,
     * seconds or millisecondsReturns the given date with the time values cleared.  */
    /**
     * Determines whether or not a date has any time values.
     *
     * @param date The date.
     * @return true iff the date is not null and any of the date's hour, minute,
     * seconds or millisecond values are greater than zero.
     */
    fun hasTime(date: Date?): Boolean {
        if (date == null) {
            return false
        }
        val c = Calendar.getInstance()
        c.time = date
        return c[Calendar.HOUR_OF_DAY] > 0 || c[Calendar.MINUTE] > 0 || c[Calendar.SECOND] > 0 || c[Calendar.MILLISECOND] > 0
    }

    // funtion check yesterday date
    fun isYesterday(d: Date): Boolean {
        return DateUtils.isToday(d.time + DateUtils.DAY_IN_MILLIS)
    }

    // function check tomorrow date
    fun isTomorrow(d: Date): Boolean {
        return DateUtils.isToday(d.time - DateUtils.DAY_IN_MILLIS)
    }

    /**
     * Returns the given date with time set to the end of the day
     */
    fun getEnd(date: Date?): Date? {
        if (date == null) {
            return null
        }
        val c = Calendar.getInstance()
        c.time = date
        c[Calendar.HOUR_OF_DAY] = 23
        c[Calendar.MINUTE] = 59
        c[Calendar.SECOND] = 59
        c[Calendar.MILLISECOND] = 999
        return c.time
    }

    /**
     * Returns the maximum of two dates. A null date is treated as being less
     * than any non-null date.
     */
    fun max(d1: Date?, d2: Date?): Date? {
        if (d1 == null && d2 == null) return null
        if (d1 == null) return d2
        if (d2 == null) return d1
        return if (d1.after(d2)) d1 else d2
    }

    /**
     * Returns the minimum of two dates. A null date is treated as being greater
     * than any non-null date.
     */
    fun min(d1: Date?, d2: Date?): Date? {
        if (d1 == null && d2 == null) return null
        if (d1 == null) return d2
        if (d2 == null) return d1
        return if (d1.before(d2)) d1 else d2
    }

    fun checktime(time: String, endtime: String): Boolean {
        val pattern = "HH:mm:ss"
        val sdf = SimpleDateFormat(pattern)
        try {
            val date1 = sdf.parse(time)
            val date2 = sdf.parse(endtime)
            return date1.after(date2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    fun currentDate(): Date {
        val calendar = Calendar.getInstance()
        return calendar.time
    }

    fun getTimeAgos(date: Date): String {
        var time = date.time
        if (time < 1000000000000L) {
            time *= 1000
        }

        val now = currentDate().time
        if (time > now || time <= 0) {
            return "in the future"
        }

        val diff = now - time
        return when {
            diff < MINUTE_MILLIS -> "beberapa detik yang lalu"
            diff < 2 * MINUTE_MILLIS -> "1 menit yang lalu"
            diff < 60 * MINUTE_MILLIS -> "${diff / MINUTE_MILLIS} menit yang lalu"
            diff < 2 * HOUR_MILLIS -> "1 jam yang lalu"
            diff < 24 * HOUR_MILLIS -> "${diff / HOUR_MILLIS} jam yang lalu"
            diff < 48 * HOUR_MILLIS -> "1 hari yang lalu"
            else -> "${diff / DAY_MILLIS} hari yang lalu"
        }
    }

    /**
     * Converts milliseconds to "x days, x hours, x mins, x secs"
     *
     * @param millis
     * The milliseconds
     * @param longFormat
     * `true` to use "seconds" and "minutes" instead of "secs" and "mins"
     * @return A string representing how long in days/hours/minutes/seconds millis is.
     */
    fun millisToString(millis: Long, longFormat: Boolean): String {
        var millis = millis
        if (millis < 60000) {
            return String.format("%s", if (longFormat) "Sesaat" else "Sesaat")
        }
        val units = arrayOf(
            "hari",
            "jam",
            if (longFormat) "menit" else "menit",
            if (longFormat) "detik" else "detik"
        )
        val times = LongArray(4)
        times[0] = TimeUnit.DAYS.convert(millis, TimeUnit.MILLISECONDS)
        millis -= TimeUnit.MILLISECONDS.convert(times[0], TimeUnit.DAYS)
        times[1] = TimeUnit.HOURS.convert(millis, TimeUnit.MILLISECONDS)
        millis -= TimeUnit.MILLISECONDS.convert(times[1], TimeUnit.HOURS)
        times[2] = TimeUnit.MINUTES.convert(millis, TimeUnit.MILLISECONDS)
        millis -= TimeUnit.MILLISECONDS.convert(times[2], TimeUnit.MINUTES)
        times[3] = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS)
        val s = StringBuilder()
        for (i in 0..3) {
            if (times[i] > 0) {
                s.append(
                    String.format(
                        "%d %s%s, ",
                        times[i],
                        units[i],
                        if (times[i] == 1L) "" else ""
                    )
                )
            }
        }
        return s.toString().substring(0, s.length - 2)
    }

    /**
     * Converts milliseconds to "x days, x hours, x mins, x secs"
     *
     * @param millis
     * The milliseconds
     * @return A string representing how long in days/hours/mins/secs millis is.
     */
    fun millisToString(millis: Long): String {
        return millisToString(millis, false)
    }

    fun getDiffTime(date1: Date, date2: Date): Long {
        var date2 = date2
        val ms: Long
        if (isSameDay(date1, date2)) {
            ms =
                date2.time - date1.time // for example date1 = 01:55, date2 = 03:55. ignore minus or not
        } else {
            if (date2.time - date1.time < 0) { // if for example date1 = 22:00, date2 = 01:55.
                val c = Calendar.getInstance()
                c.time = date2
                c.add(Calendar.DATE, 1)
                date2 = c.time

                ms = date2.time - date1.time
            } else {
                ms = date2.time - date1.time
            }
        }

        //235 minutes ~ 4 hours for (22:00 -- 01:55).
        //120 minutes ~ 2 hours for (01:55 -- 03:55).
        return TimeUnit.MILLISECONDS.convert(ms, TimeUnit.MILLISECONDS)
    }

    fun formatToYMDHMS(date: Date): String {
        try {
            val formatApi = "yyyy-MM-dd HH:mm:ss"
            val sdf = SimpleDateFormat(formatApi, Locale.getDefault())
            return sdf.format(date)
        } catch (e: Exception) {
            return ""
        }
    }

    fun addHoursCalender(time: Int, dateNow: String): Date {
        val calendar = Calendar.getInstance()
        calendar.time = parse(dateNow, FORMAT_DATE_TIME_YMDHMS)
        calendar.add(Calendar.HOUR_OF_DAY, time)
        return calendar.time
    }

    fun parseWIBtoGMT(date: String, locale: Locale = Locale.ENGLISH): Date {
        // example Thu Mar 24 12:55:27 WIB 2022 parse to Thu Mar 24 12:55:27 GMT+07:00 2022
        if (date.contains("WIB")) {
            val parseToGMT =
                parseSetLocaleDefault(date, FORMAT_DATE_TIME_WIB, FORMAT_DATE_TIME_GMT, locale)
            return parseSetLocaleDefault(parseToGMT, FORMAT_DATE_TIME_GMT, locale)
        } else {
            return parseSetLocaleDefault(date, FORMAT_DATE_TIME_GMT, locale)
        }
    }

    fun getInfoTimeUtc(format: String, date: String): String {
        val days = computeDiff(
            getDateFromStringUtc(format, date), Date()
        )[TimeUnit.DAYS] ?: 0
        val hours = computeDiff(
            getDateFromStringUtc(format, date), Date()
        )[TimeUnit.HOURS] ?: 0
        val minutes = computeDiff(
            getDateFromStringUtc(format, date), Date()
        )[TimeUnit.MINUTES] ?: 0

        if (days > 0) {
            if (days > 7) {
                return format(parse(date, FORMAT_DATE_TIME_YMDTHMSZ), FORMAT_DATE_DMY_SLASH)
            }
            return "$days hari"
        }
        if (hours > 0) {
            return "$hours jam"
        }
        if (minutes > 0) {
            return "$minutes menit"
        }

        return "Baru saja"
    }

    fun getDateFromStringUtc(pattern: String, date: String): Date {
        val parser = SimpleDateFormat(pattern, Locale("in", "ID"))
        parser.timeZone = TimeZone.getTimeZone("UTC")
        return parser.parse(date) ?: Date()
    }

    private fun computeDiff(date1: Date, date2: Date): Map<TimeUnit, Long> {
        val diffInMillis: Long = date2.time - date1.time

        // create the list
        val units: List<TimeUnit> = ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit::class.java))
        Collections.reverse(units)

        // create the result map of TimeUnit and difference
        val result: MutableMap<TimeUnit, Long> = LinkedHashMap()
        var millisRest = diffInMillis
        for (unit in units) {

            // calculate difference in millisecond
            val diff: Long = unit.convert(millisRest, TimeUnit.MILLISECONDS)
            val diffInMilliesForUnit: Long = unit.toMillis(diff)
            millisRest -= diffInMilliesForUnit

            // put the result in the map
            result[unit] = diff
        }
        return result
    }

    fun orderDateTime(date: String): String {
        try {
            val formatYes = parse(date, FORMAT_DATE_TIME_YMDHMS)
            val formatNow = parse(currentDate().toString(), FORMAT_DATE_TIME_YMDHMS)
            val isSameday = isSameDay(formatYes, formatNow)
            val isYesday = isYesterday(formatYes)

            if (isSameday) {
                return "Hari ini, ${parse(date, FORMAT_DATE_TIME_YMDHMS, FORMAT_TIME_HM)}"
            } else if (isYesday) {
                return "Kemarin, ${parse(date, FORMAT_DATE_TIME_YMDHMS, FORMAT_TIME_HM)}"
            } else {
                return parse(date, FORMAT_DATE_TIME_YMDHMS, FORMAT_DATE_EHM_LONG_DAY_DOT_TIME)
            }
        } catch (e: Exception) {
            return ""
        }
    }

    fun getDateMinus30(format: String): String {
        return try {
            val calendar = Calendar.getInstance() // create a new Calendar instance
            calendar.add(Calendar.DAY_OF_MONTH, -30) // subtract 30 days from the current date
            val date = calendar.time // get the updated date object

            format(date, format)
        } catch (e: Exception) {
            ""
        }
    }

    fun getDateNow(format: String): String {
        return try {
            val calendar = Calendar.getInstance() // create a new Calendar instance
            val date = calendar.time // get the updated date object

            format(date, format)
        } catch (e: Exception) {
            ""
        }
    }
}