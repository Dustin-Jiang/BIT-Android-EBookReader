package top.tsukino.ebookreader.utils

class ParseNumber {
    companion object {
        fun chineseToArabic(chineseNumber: String): Int? {
            val chineseNumerals = mapOf(
                "一" to 1, "二" to 2, "三" to 3, "四" to 4, "五" to 5, "六" to 6, "七" to 7, "八" to 8, "九" to 9, "十" to 10,
                "百" to 100, "千" to 1000, "万" to 10000, "零" to 0 // 包含 "零"
            )

            var result = 0
            var currentNumber = 0

            for (char in chineseNumber) {
                val numeral = chineseNumerals[char.toString()]
                if (numeral != null) {
                    if (numeral >= 10) { // 处理 十, 百, 千, 万
                        if (currentNumber == 0) currentNumber = 1 //  例如 "十", 相当于 "一十"
                        result += currentNumber * numeral
                        currentNumber = 0
                    } else {
                        currentNumber = currentNumber * 10 + numeral // 处理个位数和十位数中的个位数部分
                    }
                } else {
                    return null // 遇到无法识别的中文数字字符
                }
            }
            result += currentNumber // 加上最后累积的个位数

            return result
        }
    }
}