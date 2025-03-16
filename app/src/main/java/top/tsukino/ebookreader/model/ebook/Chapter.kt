package top.tsukino.ebookreader.model.ebook

import top.tsukino.ebookreader.utils.ParseNumber

class Chapter(
    val title: String,
    val index: Int,
    var contentId: Long = 0,
    var id: Long = 0
) {
    companion object {
        fun isTitle(line: String): Boolean {
            return line.trim().matches(Regex("^(第[一二三四五六七八九十百千万]+章|第\\d+章|Chapter\\s+\\d+)(.*)?$"))
        }
        fun parse(title: String): Chapter? {
            // 正则表达式用于匹配多种章节标题格式
            // Group 1: 整个章节序号部分 (例如: "第一章", "第1章", "Chapter 1")
            // Group 2: 中文数字 (例如: "一", "二", "三"...)
            // Group 4: "第" 后面跟着的阿拉伯数字 (例如: "1", "2", "3"...)
            // Group 5: "Chapter" 后面跟着的阿拉伯数字 (例如: "1", "2", "3"...)
            // Group 6: 可选的章节描述性标题
            val chapterTitleRegex = Regex("^(第([一二三四五六七八九十百千万]+)章|第(\\d+)章|Chapter\\s+(\\d+))\\s*(.*)?$")
            val matchResult = chapterTitleRegex.find(title.trim())

            if (matchResult != null) {
                val groups = matchResult.groups

                var index: Int? = null
                var descriptionTitle: String? = groups[groups.size - 1]?.value?.trim() // 获取最后一个捕获组作为描述性标题

                // 确定章节序号的来源 (中文数字, 阿拉伯数字 - "第" 开头, 阿拉伯数字 - "Chapter" 开头)
                when {
                    groups[2]?.value != null -> {
                        // 中文数字
                        index = ParseNumber.chineseToArabic(groups[2]!!.value)
                    }
                    groups[4]?.value != null -> {
                        // "第" + 阿拉伯数字
                        index = groups[4]!!.value.toIntOrNull()
                    }
                    groups[5]?.value != null -> {
                        // "Chapter" + 阿拉伯数字
                        index = groups[5]!!.value.toIntOrNull()
                    }
                }
                if (index != null) {
                    return Chapter(
                        descriptionTitle.takeIf { !it.isNullOrEmpty() } ?: "Chapter $index", // 使用 takeIf 避免空字符串变成 null
                        index
                    )
                }
            }
            return null // 如果不匹配，返回 null
        }


    }

    override fun toString(): String {
        return "Chapter $index: $title";
    }
}