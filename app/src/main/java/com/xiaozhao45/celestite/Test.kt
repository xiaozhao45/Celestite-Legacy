package com.xiaozhao45.celestite

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/*
我都忘了还有这个。。。
废弃代码别看。

xiaozhao45 2025/07/10
 */


fun parseQimenResult(result: String): List<Palace> {
    val palaces = mutableListOf<Palace>()

    // 按照九宫格顺序：巽离坤，震中兑，艮坎乾
    val palaceNames = listOf("巽", "离", "坤", "震", "中", "兑", "艮", "坎", "乾")

    // 初始化九个宫位
    for (name in palaceNames) {
        val palace = Palace()
        palace.name = name
        palaces.add(palace)
    }

    // 解析排盘数据
    val lines = result.split("\n")
    var isInGrid = false
    val gridLines = mutableListOf<String>()

    // 提取九宫格部分
    for (line in lines) {
        if (line.contains("| 九天")) {
            isInGrid = true
        }
        if (isInGrid && line.contains("|")) {
            gridLines.add(line)
        }
        if (line.contains("===========================================") && isInGrid) {
            break
        }
    }

    // 解析网格数据 - 按照排盘格式解析
    // 第一行: 巽(4) 离(9) 坤(2)
    if (gridLines.size >= 1) {
        parseGridLine(gridLines[0], palaces, listOf(3, 8, 1)) // 巽离坤对应索引
    }
    if (gridLines.size >= 2) {
        parseGridLine(gridLines[1], palaces, listOf(3, 8, 1)) // 九星行
    }
    if (gridLines.size >= 3) {
        parseGridLine(gridLines[2], palaces, listOf(3, 8, 1)) // 八门行
    }

    // 第二行: 震(3) 中(5) 兑(7)
    if (gridLines.size >= 5) {
        parseGridLine(gridLines[4], palaces, listOf(2, 4, 5)) // 震中兑对应索引
    }
    if (gridLines.size >= 6) {
        parseGridLine(gridLines[5], palaces, listOf(2, 4, 5)) // 九星行
    }
    if (gridLines.size >= 7) {
        parseGridLine(gridLines[6], palaces, listOf(2, 4, 5)) // 八门行
    }

    // 第三行: 艮(8) 坎(1) 乾(6)
    if (gridLines.size >= 9) {
        parseGridLine(gridLines[8], palaces, listOf(6, 7, 8)) // 艮坎乾对应索引
    }
    if (gridLines.size >= 10) {
        parseGridLine(gridLines[9], palaces, listOf(6, 7, 8)) // 九星行
    }
    if (gridLines.size >= 11) {
        parseGridLine(gridLines[10], palaces, listOf(6, 7, 8)) // 八门行
    }

    // 解析空亡和驿马标记
    parseSpecialMarks(result, palaces)

    return palaces
}

private fun parseGridLine(line: String, palaces: MutableList<Palace>, indices: List<Int>) {
    val parts = line.split("|").filter { it.trim().isNotEmpty() }

    for (i in parts.indices) {
        if (i < indices.size && indices[i] < palaces.size) {
            val content = parts[i].trim()
            val palace = palaces[indices[i]]

            // 根据内容类型设置相应属性
            when {
                // 八神
                content.contains("九天") || content.contains("九地") || content.contains("玄武") ||
                        content.contains("白虎") || content.contains("腾蛇") || content.contains("太阴") ||
                        content.contains("六合") || content.contains("值符") -> {
                    palace.upMystery = extractFirstWord(content)
                }

                // 九星
                content.contains("天英") || content.contains("天芮") || content.contains("天柱") ||
                        content.contains("天辅") || content.contains("天心") || content.contains("天冲") ||
                        content.contains("天任") || content.contains("天蓬") || content.contains("天禽") -> {
                    palace.star = extractStarName(content)
                    // 提取天干
                    val stems = extractStems(content)
                    if (stems.isNotEmpty()) {
                        palace.upStem = stems[0]
                        if (stems.size > 1) {
                            palace.downStem = stems[1]
                        }
                    }
                }

                // 八门
                content.contains("开门") || content.contains("休门") || content.contains("生门") ||
                        content.contains("伤门") || content.contains("杜门") || content.contains("景门") ||
                        content.contains("死门") || content.contains("惊门") -> {
                    palace.door = extractDoorName(content)
                    // 提取天干
                    val stems = extractStems(content)
                    if (stems.isNotEmpty() && palace.downStem == null) {
                        palace.downStem = stems[0]
                    }
                }
            }
        }
    }
}

private fun extractFirstWord(content: String): String {
    val words = listOf("九天", "九地", "玄武", "白虎", "腾蛇", "太阴", "六合", "值符")
    return words.find { content.contains(it) } ?: ""
}

private fun extractStarName(content: String): String {
    val stars = listOf("天英", "天芮", "天柱", "天辅", "天心", "天冲", "天任", "天蓬", "天禽")
    return stars.find { content.contains(it) } ?: ""
}

private fun extractDoorName(content: String): String {
    val doors = listOf("开门", "休门", "生门", "伤门", "杜门", "景门", "死门", "惊门")
    return doors.find { content.contains(it) } ?: ""
}

private fun extractStems(content: String): List<String> {
    val stems = listOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    return stems.filter { content.contains(it) }
}

private fun parseSpecialMarks(result: String, palaces: MutableList<Palace>) {
    // 解析空亡标记 (空)
    if (result.contains("(空)")) {
        // 根据排盘中的空亡标记设置相应宫位的death属性
        val lines = result.split("\n")
        for (line in lines) {
            if (line.contains("(空)")) {
                // 这里需要根据具体位置确定是哪个宫位空亡
                // 从示例看，九地和玄武位置有空亡标记
            }
        }
    }

    // 解析驿马标记 (马空)
    if (result.contains("(马空)")) {
        // 设置相应宫位的active属性
    }
}

// 使用示例
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun mainPreview() {
    val result = """===========================================
感谢奇门遁甲排盘程序CQM的开发者：taynpg (Gitee名称)
本排盘程序使用了此程序作为内核。公元:2025年07月09日05时02分00秒 遁甲排盘cqm
===========================================
农历:二零二五年 六月十五日 卯时
干支:乙 癸 己 丁
     巳 未 卯 卯             (转盘超接置润)
===========================================
值符:天任  值使:生门   [小暑上元][阴遁八局]
===========================================
| 太阴   (马) | 腾蛇        | 值符        |
| 天心    庚  | 天蓬    丙  |*天任    戊  |
| 休门    壬  |*生门    乙  | 伤门    丁  |
===========================================
| 六合        |             | 九天        |
| 天柱   [己] |      辛     | 天冲    癸  |
| 开门    癸  |             | 杜门    己  |
===========================================
| 白虎        | 玄武        | 九地   (空) |
| 芮禽    丁辛| 天英    乙  | 天辅    壬  |
| 惊门    戊  | 死门    丙  | 景门    庚  |
===========================================
[07-07 小暑 04:04:43] [07-22 大暑 21:29:11]
===========================================
===========================================
=Row Result===================================
===========================================
定义甲甲=空，因为甲甲只出现在中五宫，而中五宫没有任何天盘天干，
定义甲+{天干}的天盘干为去除甲后的天干。[
  {
    "name": "坎1宫",
    "code": 1,
    "upStem": "甲乙",
    "downStem": "丙",
    "door": "死门",
    "star": "天英",
    "upMystery": "玄武",
    "active": false,
    "death": false
  },
  {
    "name": "坤2宫",
    "code": 2,
    "upStem": "甲戊",
    "downStem": "丁",
    "door": "伤门",
    "star": "天任",
    "upMystery": "值符",
    "active": false,
    "death": false
  },
  {
    "name": "震3宫",
    "code": 3,
    "upStem": "甲己",
    "downStem": "癸",
    "door": "开门",
    "star": "天柱",
    "upMystery": "六合",
    "active": false,
    "death": false
  },
  {
    "name": "巽4宫",
    "code": 4,
    "upStem": "甲庚",
    "downStem": "壬",
    "door": "休门",
    "star": "天心",
    "upMystery": "太阴",
    "active": true,
    "death": false
  },
  {
    "name": "中5宫",
    "code": 5,
    "upStem": "甲甲",
    "downStem": "辛",
    "door": "九天",
    "star": "天英",
    "upMystery": "",
    "active": false,
    "death": false
  },
  {
    "name": "乾6宫",
    "code": 6,
    "upStem": "甲壬",
    "downStem": "庚",
    "door": "景门",
    "star": "天辅",
    "upMystery": "九地",
    "active": false,
    "death": true
  },
  {
    "name": "兑7宫",
    "code": 7,
    "upStem": "甲癸",
    "downStem": "己",
    "door": "杜门",
    "star": "天冲",
    "upMystery": "九天",
    "active": false,
    "death": false
  },
  {
    "name": "艮8宫",
    "code": 8,
    "upStem": "丁辛",
    "downStem": "戊",
    "door": "惊门",
    "star": "天芮",
    "upMystery": "白虎",
    "active": false,
    "death": false
  },
  {
    "name": "离9宫",
    "code": 9,
    "upStem": "甲丙",
    "downStem": "乙",
    "door": "生门",
    "star": "天蓬",
    "upMystery": "腾蛇",
    "active": false,
    "death": false
  }
]
"""

    ForecastScreen(result = result)
}