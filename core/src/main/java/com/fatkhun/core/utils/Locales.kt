package com.fatkhun.core.utils

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import java.util.TreeMap
import kotlin.toString

object Locales {

    private val FORMAT_MONEY = DecimalFormatSymbols()
    private val FORMAT_MONEY_WITHOUT_CURRENCY = DecimalFormatSymbols()
    const val PATTERN_MILLION = "#,###,###"

    init {
        FORMAT_MONEY.currencySymbol = "Rp "
        FORMAT_MONEY.groupingSeparator = '.'
        FORMAT_MONEY.monetaryDecimalSeparator = ','

        FORMAT_MONEY_WITHOUT_CURRENCY.currencySymbol = ""
        FORMAT_MONEY_WITHOUT_CURRENCY.groupingSeparator = '.'
        FORMAT_MONEY_WITHOUT_CURRENCY.monetaryDecimalSeparator = ','
    }

    fun money(raw: String): String? {
        return money(raw.let { toDouble(it, 0.0) })
    }

    fun money(raw: String, currency: String): String? {
        return money(raw.let { toDouble(it, 0.0) }, currency)
    }

    fun money(raw: Double): String? {
        return money(raw, "Rp ")
    }

    fun money(raw: Double, currency: String): String? {
        return money(raw, "#,###,###.-", currency)
    }

    fun money(raw: Double, pattern: String?, currency: String): String? {
        val myFormatter = DecimalFormat(
            pattern,
            DecimalFormatSymbols.getInstance(Locale.getDefault())
        )
        return currency + myFormatter.format(raw)
    }

    fun normalisasiNumberFormat(
        raw: Float,
        pattern: String = "#.#",
        roundingMode: RoundingMode = RoundingMode.DOWN
    ): String {
        val regexNumber = trimTrailingZero(raw.toString()).replace("\\D".toRegex(), "")
        //logError("reg $regexNumber")
        return if (regexNumber.toInt().length() == 1) {
            toFloat(raw, 0f).toString()
        } else {
            numberFormat(raw, pattern, roundingMode)
        }
    }

    fun numberFormat(
        raw: Float,
        pattern: String = "#.#",
        roundingMode: RoundingMode = RoundingMode.DOWN
    ): String {
        val myFormatter = DecimalFormat(
            pattern,
            DecimalFormatSymbols.getInstance(Locale.getDefault())
        )
        myFormatter.roundingMode = roundingMode
        return myFormatter.format(raw)
    }

    fun number_format(raw: Double): String {
        val myFormatter = DecimalFormat(
            PATTERN_MILLION,
            DecimalFormatSymbols.getInstance(Locale.getDefault())
        )
        return myFormatter.format(raw)
    }

    fun parse(raw: String): Double? {
        val myFormatter = DecimalFormat(
            "#,###,###.00",
            DecimalFormatSymbols.getInstance(Locale.getDefault())
        )
        try {
            return myFormatter.parse(raw)?.let { toDouble(it, 0.0) }
        } catch (e: Exception) {
            return myFormatter.parse("0.0")?.let { toDouble(it, 0.0) }
        }
    }

    fun moneyNoFraction(raw: String?, usingCurrency: Boolean = true): String? {
        /*
        solusi
        val df = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        */
        val df = NumberFormat.getCurrencyInstance()
        df.maximumFractionDigits = 0
        /*
        solusi:
        (df as DecimalFormat).decimalFormatSymbols =
            if (usingCurrency)
                df.decimalFormatSymbols.apply { currencySymbol = "Rp" }
            else
                df.decimalFormatSymbols.apply { currencySymbol = "" }
        */
        (df as DecimalFormat).decimalFormatSymbols =
            if (usingCurrency) FORMAT_MONEY else FORMAT_MONEY_WITHOUT_CURRENCY
        try {
            return df.format(raw?.let { toDouble(it, 0.0) })
        } catch (e: Exception) {
            return df.format(0.0)
        }
    }

    fun moneyNoFraction(raw: String?, usingCurrency: Boolean = true, maxDigit: Int = 0): String? {
        val df = NumberFormat.getCurrencyInstance()
        df.maximumFractionDigits = maxDigit
        (df as DecimalFormat).decimalFormatSymbols =
            if (usingCurrency) FORMAT_MONEY else FORMAT_MONEY_WITHOUT_CURRENCY
        try {
            return df.format(raw?.let { toDouble(it, 0.0) })
        } catch (e: Exception) {
            return df.format(0.0)
        }
    }

    fun humanReadableByteCount(bytes: Long, si: Boolean): String? {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + if (si) "" else "i"
        return String.format(
            Locale.US,
            "%.1f %sB",
            bytes / Math.pow(unit.toDouble(), exp.toDouble()),
            pre
        )
    }

    /**
     * Exact code: String fixedNumber = cleanNumber(number).replace(".", ""); return
     * StringHelper.insertIntoString(fixedNumber, char0, "", "", char3, "", "", char6);
     *
     * @param number
     * @param char0
     * @param char3
     * @param char6
     * @return
     */
//    public static String formatPhoneNumber(String number, String char0, String char3, String char6) {
//        String fixedNumber = Numbers.cleanNumber(number).replace(".", "");
//        return Strings.insertIntoString(fixedNumber, char0, "", "", char3, "", "", char6);
//    }

    /**
     * Returns: formatPhoneNumber(number, "(", ") ", "-") which formats a number to look like this: (555)
     * 123-4567. Note: If you give too many numbers they'll just be added to the end of the number. Like this
     * (555) 123-456789101112
     *
     * @param number
     * @return formatted phone number
     */
//    public static String formatPhoneNumber(String number) {
//        return formatPhoneNumber(number, "(", ") ", "-");
//    }

    /**
     * If the call was formatCreditCard("1234567812345678", "-") the result would be: 1234-5678-1234-5678. This
     * will continue if you give too long or too short of a number. If you give 12345, the result will be:
     * 1234-5. If you give 12345678123456789, it will return 1234-5678-1234-5678-9. NOTE: Also first calls
     * NumberHelper.cleanNumber(creditCard).replace(".", "");
     *
     * @param creditCard the number string to format like a credit card number
     * @param separator  the item to put between every 4 numbers
     * @return formatted credit card
     */
//    public static String formatCreditCard(String creditCard, String separator) {
//        String fixedCreditCard = Numbers.cleanNumber(creditCard).replace(".", "");
//        char[] numbers = fixedCreditCard.toCharArray();
//        //1234567812345678
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < numbers.length; i++) {
//            if (i % 4 == 0 && i > 0) {
//                sb.append(separator);
//            }
//            sb.append(numbers[i]);
//        }
//        return sb.toString();
//    }

    /**
     * Exact code: String fixedNumber = cleanNumber(number).replace(".", ""); return
     * StringHelper.insertIntoString(fixedNumber, char0, "", "", char3, "", "", char6);
     *
     * @param number
     * @param char0
     * @param char3
     * @param char6
     * @return
     */
//    public static String formatPhoneNumber(String number, String char0, String char3, String char6) {
//        String fixedNumber = Numbers.cleanNumber(number).replace(".", "");
//        return Strings.insertIntoString(fixedNumber, char0, "", "", char3, "", "", char6);
//    }
    /**
     * Returns: formatPhoneNumber(number, "(", ") ", "-") which formats a number to look like this: (555)
     * 123-4567. Note: If you give too many numbers they'll just be added to the end of the number. Like this
     * (555) 123-456789101112
     *
     * @param number
     * @return formatted phone number
     */
//    public static String formatPhoneNumber(String number) {
//        return formatPhoneNumber(number, "(", ") ", "-");
//    }
    /**
     * If the call was formatCreditCard("1234567812345678", "-") the result would be: 1234-5678-1234-5678. This
     * will continue if you give too long or too short of a number. If you give 12345, the result will be:
     * 1234-5. If you give 12345678123456789, it will return 1234-5678-1234-5678-9. NOTE: Also first calls
     * NumberHelper.cleanNumber(creditCard).replace(".", "");
     *
     * @param creditCard the number string to format like a credit card number
     * @param separator  the item to put between every 4 numbers
     * @return formatted credit card
     */
//    public static String formatCreditCard(String creditCard, String separator) {
//        String fixedCreditCard = Numbers.cleanNumber(creditCard).replace(".", "");
//        char[] numbers = fixedCreditCard.toCharArray();
//        //1234567812345678
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < numbers.length; i++) {
//            if (i % 4 == 0 && i > 0) {
//                sb.append(separator);
//            }
//            sb.append(numbers[i]);
//        }
//        return sb.toString();
//    }
    /**
     * This is posting something in a URL. It replaces anything in the string given with percentEncoded values
     *
     * @param originalString
     * @return
     */
    fun percentEncodeString(originalString: String): String? {
        //It is important that % is first, if you put it any later it will replace % with anything that's replace before.
        //Try it, you'll see what I mean.
        val replaceMap: MutableMap<Char, String> = TreeMap()
        //<editor-fold defaultstate="collapsed" desc="Set Map">
        replaceMap['%'] = "%25"
        replaceMap['!'] = "%21"
        replaceMap['*'] = "%2A"
        replaceMap['\''] = "%27" //Escape character. This value is: '
        replaceMap['('] = "%28"
        replaceMap[')'] = "%29"
        replaceMap[';'] = "%3B"
        replaceMap[':'] = "%3A"
        replaceMap['@'] = "%40"
        replaceMap['&'] = "%26"
        replaceMap['='] = "%3D"
        replaceMap['+'] = "%2B"
        replaceMap['$'] = "%24"
        replaceMap[','] = "%2C"
        replaceMap['/'] = "%2F"
        replaceMap['?'] = "%3F"
        replaceMap['#'] = "%23"
        replaceMap['['] = "%5B"
        replaceMap[']'] = "%5D"
        replaceMap[' '] = "%20"
        //</editor-fold>
        val charArry = originalString.toCharArray()
        val fixedString = StringBuilder()
        for (c in charArry) {
            val replacement = replaceMap[c]
            if (replacement != null) {
                fixedString.append(replacement)
            } else {
                fixedString.append(c)
            }
        }
        return fixedString.toString()
    }

    fun capitalize(s: String?): String {
        if (s == null || s.length == 0) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first).toString() + s.substring(1)
        }
    }

    fun UcWords(s: String): String? {
        val res = StringBuilder()
        for (sp in s.split(" ").toTypedArray()) {
            res.append(capitalize(sp)).append(" ")
        }
        return res.toString().trim { it <= ' ' }
    }

    fun replaceRegexOnlyNumeric(num: String): String {
        return num.replace("\\D".toRegex(), "")
    }

    fun replaceRegexOnlyWhitespace(text: String): String {
        return text.replace("(?<=\\w)\\s+(?=\\w)|(\\s+)".toRegex(), "")
    }
}