package com.xiaozhao45.celestite

import android.os.Build
import androidx.annotation.RequiresApi
import com.tyme.sixtycycle.*
import com.tyme.solar.SolarDay
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.tyme.solar.SolarTerm
import com.tyme.solar.SolarTime
import java.time.LocalDateTime

/*
废弃代码别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看

气死我了，奇门遁甲有点复杂了————来自一个妄想实现离线奇门遁甲算法的人xiaozhao45 2025/07/10
*/

/*
废弃代码别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看

废弃代码别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看

废弃代码别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看

废弃代码别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看

废弃代码别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看

废弃代码别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看

废弃代码别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看

废弃代码别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看

废弃代码别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看别看

xiaozhao45 2025/07/10
 */



@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun CalcSrceen(){
    TopAppBar(
        title = { Text("演算参数") }
    )
    Column {

    }
}



fun SixtyCycleFormatted(year:Int,month:Int,day:Int,hour:Int,minute:Int,sec:Int): String {
//    var FormattedYear = SixtyCycleYear(year)
//    var FormattedDay = SixtyCycleDay(SolarDay(year, month, day))
//    var FormattedMonth = FormattedDay.sixtyCycleMonth
    var FormattedHour = SolarTime.fromYmdHms(year,month,day,hour,minute,sec).sixtyCycleHour
    var Result = insertSpaceEveryThreeChars(FormattedHour.toString())
    return "$Result"
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewSixtyCycleFormatted(){
    var Time = LocalDateTime.now()
    var Fmtted = SixtyCycleFormatted(
        Time.year,
        Time.monthValue,
        Time.dayOfMonth,
        Time.hour,
        Time.minute,
        Time.second
    )
    Text(Fmtted)
}

fun insertSpaceEveryThreeChars(str: String): String {
    val sb = StringBuilder()
    for (i in str.indices) {
        if (i != 0 && i % 3 == 0) {
            sb.append(' ') // 每三个字符插入一个空格
        }
        sb.append(str[i])
    }
    return sb.toString()
}

class SixC {
    var yearStem:String? = null
    var yearEarthly:String? = null

    var monthStem:String? = null
    var monthEarthly:String? = null

    var dayStem:String? = null
    var dayEarthly:String? = null

    var timeStem:String? = null
    var timeEarthly:String? = null
}

// 扩展函数：解析字符串并填充到 SixC 对象
fun SixC.parseFrom(input: String): SixC {
    val heavenlyStems = "甲乙丙丁戊己庚辛壬癸"
    val earthlyBranches = "子丑寅卯辰巳午未申酉戌亥"

    // 分割输入字符串
    val parts = input.split("\\s+".toRegex()) // 按空白分割
    if (parts.size != 4) throw IllegalArgumentException("输入格式错误，应为：XX年 XX月 XX日 XX时")

    val labels = arrayOf("year", "month", "day", "time")
    val pairs = arrayOf(
        Pair(::yearStem::set, ::yearEarthly::set),
        Pair(::monthStem::set, ::monthEarthly::set),
        Pair(::dayStem::set, ::dayEarthly::set),
        Pair(::timeStem::set, ::timeEarthly::set)
    )

    for ((index, part) in parts.withIndex()) {
        if (part.length < 2) throw IllegalArgumentException("${labels[index]} 部分长度不足：$part")

        val stemChar = part[0].toString()
        val earthlyChar = part[1].toString()

        if (!heavenlyStems.contains(stemChar)) throw IllegalArgumentException("非法天干字符：$stemChar")
        if (!earthlyBranches.contains(earthlyChar)) throw IllegalArgumentException("非法地支字符：$earthlyChar")

        pairs[index].first(stemChar)
        pairs[index].second(earthlyChar)
    }

    return this
}

class Palace_Calc {
    var name: String? = null
        set(value) {
            field = value
            code = when (value) {
                "坎" -> 1
                "坤" -> 2
                "震" -> 3
                "巽" -> 4
                "中" -> 5
                "乾" -> 6
                "兑" -> 7
                "艮" -> 8
                "离" -> 9
                else -> null
            }
        }

    var code: Int? = null
        private set

    // 天盘天干
    var upStem: String? = null
    // 地盘天干
    var downStem: String? = null


    // 地支 - 根据宫位代码自动计算
    val earthly: List<String>
        get() = when (code) {
            1 -> listOf("子")
            2 -> listOf("未", "申")
            3 -> listOf("卯")
            4 -> listOf("辰", "巳")
            5 -> listOf("无")
            6 -> listOf("戌", "亥")
            7 -> listOf("酉")
            8 -> listOf("丑", "寅")
            9 -> listOf("午")
            else -> emptyList()
        }

    // 八神
    var upMystery: String? = null

    // 八门
    var door: String? = null

    // 九星
    var star: String? = null

    // 空亡和驿马
    var death: Boolean? = null
    var active: Boolean? = null

    // 获取指定属性值的方法
    fun get(property: String): Any? {
        return when (property) {
            "name" -> name
            "code" -> code
            "upStem" -> upStem
            "downStem" -> downStem
            "earthly" -> earthly
            "upMystery" -> upMystery
            "door" -> door
            "star" -> star
            "death" -> death
            "active" -> active
            else -> null
        }
    }

    // 设置指定属性值的方法
    fun set(property: String, value: Any?) {
        when (property) {
            "name" -> name = value as? String
            "upStem" -> upStem = value as? String
            "downStem" -> downStem = value as? String
            "upMystery" -> upMystery = value as? String
            "door" -> door = value as? String
            "star" -> star = value as? String
            "death" -> death = value as? Boolean
            "active" -> active = value as? Boolean
        }
    }

    override fun toString(): String {
        return "Palace_Calc(name='$name', code=$code, upStem='$upStem', downStem='$downStem', " +
                "door='$door', star='$star', upMystery='$upMystery')"
    }
}

class NinePalace_Calc {
    private val Palace_Calcs = mutableMapOf<Int, Palace_Calc>()

    init {
        // 初始化九宫
        val Palace_CalcNames = listOf("坎", "坤", "震", "巽", "中", "乾", "兑", "艮", "离")
        Palace_CalcNames.forEachIndexed { index, name ->
            val Palace_Calc = Palace_Calc()
            Palace_Calc.name = name
            Palace_Calcs[index + 1] = Palace_Calc
        }
    }

    // 根据宫位代码获取宫位
    fun getPalace_Calc(code: Int): Palace_Calc? = Palace_Calcs[code]

    // 根据宫位名称获取宫位
    fun getPalace_Calc(name: String): Palace_Calc? {
        return Palace_Calcs.values.find { it.name == name }
    }

    // 获取所有宫位
    fun getAllPalace_Calcs(): List<Palace_Calc> = Palace_Calcs.values.toList()

    // 设置特定宫位的属性
    fun setPalace_CalcProperty(Palace_CalcCode: Int, property: String, value: Any?) {
        Palace_Calcs[Palace_CalcCode]?.set(property, value)
    }

    // 获取特定宫位的属性
    fun getPalace_CalcProperty(Palace_CalcCode: Int, property: String): Any? {
        return Palace_Calcs[Palace_CalcCode]?.get(property)
    }

    // 批量设置宫位信息
    fun setupPalace_Calcs(setup: (Palace_Calc) -> Unit) {
        Palace_Calcs.values.forEach(setup)
    }

    override fun toString(): String {
        return Palace_Calcs.values.joinToString("\n") { it.toString() }
    }
}

fun getTenDayPeriodFirst(dayStem:String, dayEarthly:String): String {
    val day = "$dayStem$dayEarthly"

    return when (day) {
        // 甲子旬（第1行）
        "甲子", "乙丑", "丙寅", "丁卯", "戊辰", "己巳", "庚午", "辛未", "壬申", "癸酉" -> "甲子"

        // 甲戌旬（第2行）
        "甲戌", "乙亥", "丙子", "丁丑", "戊寅", "己卯", "庚辰", "辛巳", "壬午", "癸未" -> "甲戌"

        // 甲申旬（第3行）
        "甲申", "乙酉", "丙戌", "丁亥", "戊子", "己丑", "庚寅", "辛卯", "壬辰", "癸巳" -> "甲申"

        // 甲午旬（第4行）
        "甲午", "乙未", "丙申", "丁酉", "戊戌", "己亥", "庚子", "辛丑", "壬寅", "癸卯" -> "甲午"

        // 甲辰旬（第5行）
        "甲辰", "乙巳", "丙午", "丁未", "戊申", "己酉", "庚戌", "辛亥", "壬子", "癸丑" -> "甲辰"

        // 甲寅旬（第6行）
        "甲寅", "乙卯", "丙辰", "丁巳", "戊午", "己未", "庚申", "辛酉", "壬戌", "癸亥" -> "甲寅"

        else -> throw IllegalArgumentException("unreachable : $day")
    }
}

fun putDownStem(day: Int, month: Int, year: Int, sixC: SixC): NinePalace_Calc {
    val solarTerm = SolarDay.fromYmd(year, month, day).term
    val yinyang = when (solarTerm.toString()) {
        // 阳遁：冬至到夏至前（阳气上升期）
        "冬至", "小寒", "大寒", "立春", "雨水", "惊蛰",
        "春分", "清明", "谷雨", "立夏", "小满", "芒种" -> "阳遁"

        // 阴遁：夏至到冬至前（阴气上升期）
        "夏至", "小暑", "大暑", "立秋", "处暑", "白露",
        "秋分", "寒露", "霜降", "立冬", "小雪", "大雪" -> "阴遁"

        else -> throw IllegalArgumentException("无效的节气：${solarTerm}")
    }

    val gameNumber = sixC.dayEarthly?.let { dayEarthly ->
        sixC.dayStem?.let { dayStem ->
            getGameNumber(dayStem, dayEarthly, solarTerm.toString())
        }
    } ?: throw IllegalArgumentException("日干支信息不完整")

    val result = NinePalace_Calc()

    // 地盘天干排列顺序（戊开始，甲不用）
    val stemOrder = listOf("戊", "己", "庚", "辛", "壬", "癸", "丁", "丙", "乙")

    // 九宫顺序（包括中宫）
    val Palace_CalcOrder = if (yinyang == "阳遁") {
        // 阳遁：顺宫数方向
        listOf(1, 2, 3, 4, 5, 6, 7, 8, 9) // 坎坤震巽中乾兑艮离
    } else {
        // 阴遁：逆宫数方向
        listOf(9, 8, 7, 6, 5, 4, 3, 2, 1) // 坎艮兑乾中巽震坤离
    }

    // 找到局数宫在宫位序列中的索引
    val startIndex = Palace_CalcOrder.indexOf(gameNumber)
    if (startIndex == -1) {
        throw IllegalArgumentException("局数宫位无效：$gameNumber")
    }

    // 按顺序排列天干
    for (i in stemOrder.indices) {
        val stem = stemOrder[i]
        val Palace_CalcIndex = (startIndex + i) % Palace_CalcOrder.size
        val Palace_CalcCode = Palace_CalcOrder[Palace_CalcIndex]
        result.setPalace_CalcProperty(Palace_CalcCode, "downStem", stem)
    }

    return result
}

fun getGameNumber(dayStem: String, dayEarthly: String, solarTerm: String): Int {
    val day = "$dayStem$dayEarthly"

    // 先确定属于上中下哪一元
    val yuan = when (day) {
        // 上元
        "甲子", "乙丑", "丙寅", "丁卯", "戊辰",
        "己卯", "庚辰", "辛巳", "壬午", "癸未",
        "甲午", "乙未", "丙申", "丁酉", "戊戌",
        "己酉", "庚戌", "辛亥", "壬子", "癸丑" -> "上元"

        // 中元
        "己巳", "庚午", "辛未", "壬申", "癸酉",
        "甲申", "乙酉", "丙戌", "丁亥", "戊子",
        "己亥", "庚子", "辛丑", "壬寅", "癸卯",
        "甲寅", "乙卯", "丙辰", "丁巳", "戊午" -> "中元"

        // 下元
        "甲戌", "乙亥", "丙子", "丁丑", "戊寅",
        "己丑", "庚寅", "辛卯", "壬辰", "癸巳",
        "甲辰", "乙巳", "丙午", "丁未", "戊申",
        "己未", "庚申", "辛酉", "壬戌", "癸亥" -> "下元"

        else -> throw IllegalArgumentException("无效的干支组合：$day")
    }

    // 根据节气和元数确定局数
    return when (solarTerm) {
        // 阳遁
        "冬至" -> when (yuan) { "上元" -> 1; "中元" -> 7; "下元" -> 4; else -> throw IllegalArgumentException() }
        "小寒" -> when (yuan) { "上元" -> 2; "中元" -> 8; "下元" -> 5; else -> throw IllegalArgumentException() }
        "大寒" -> when (yuan) { "上元" -> 3; "中元" -> 9; "下元" -> 6; else -> throw IllegalArgumentException() }
        "立春" -> when (yuan) { "上元" -> 8; "中元" -> 5; "下元" -> 6; else -> throw IllegalArgumentException() }
        "雨水" -> when (yuan) { "上元" -> 9; "中元" -> 6; "下元" -> 3; else -> throw IllegalArgumentException() }
        "惊蛰" -> when (yuan) { "上元" -> 1; "中元" -> 7; "下元" -> 4; else -> throw IllegalArgumentException() }
        "春分" -> when (yuan) { "上元" -> 3; "中元" -> 9; "下元" -> 6; else -> throw IllegalArgumentException() }
        "清明" -> when (yuan) { "上元" -> 4; "中元" -> 1; "下元" -> 7; else -> throw IllegalArgumentException() }
        "谷雨" -> when (yuan) { "上元" -> 5; "中元" -> 2; "下元" -> 8; else -> throw IllegalArgumentException() }
        "立夏" -> when (yuan) { "上元" -> 4; "中元" -> 1; "下元" -> 7; else -> throw IllegalArgumentException() }
        "小满" -> when (yuan) { "上元" -> 5; "中元" -> 2; "下元" -> 8; else -> throw IllegalArgumentException() }
        "芒种" -> when (yuan) { "上元" -> 6; "中元" -> 3; "下元" -> 9; else -> throw IllegalArgumentException() }

        // 阴遁
        "夏至" -> when (yuan) { "上元" -> 9; "中元" -> 3; "下元" -> 6; else -> throw IllegalArgumentException() }
        "小暑" -> when (yuan) { "上元" -> 8; "中元" -> 2; "下元" -> 5; else -> throw IllegalArgumentException() }
        "大暑" -> when (yuan) { "上元" -> 7; "中元" -> 1; "下元" -> 4; else -> throw IllegalArgumentException() }
        "立秋" -> when (yuan) { "上元" -> 2; "中元" -> 5; "下元" -> 8; else -> throw IllegalArgumentException() }
        "处暑" -> when (yuan) { "上元" -> 1; "中元" -> 4; "下元" -> 7; else -> throw IllegalArgumentException() }
        "白露" -> when (yuan) { "上元" -> 9; "中元" -> 6; "下元" -> 3; else -> throw IllegalArgumentException() }
        "秋分" -> when (yuan) { "上元" -> 7; "中元" -> 1; "下元" -> 4; else -> throw IllegalArgumentException() }
        "寒露" -> when (yuan) { "上元" -> 6; "中元" -> 9; "下元" -> 2; else -> throw IllegalArgumentException() }
        "霜降" -> when (yuan) { "上元" -> 5; "中元" -> 8; "下元" -> 3; else -> throw IllegalArgumentException() }
        "立冬" -> when (yuan) { "上元" -> 6; "中元" -> 9; "下元" -> 5; else -> throw IllegalArgumentException() }
        "小雪" -> when (yuan) { "上元" -> 5; "中元" -> 8; "下元" -> 2; else -> throw IllegalArgumentException() }
        "大雪" -> when (yuan) { "上元" -> 4; "中元" -> 7; "下元" -> 1; else -> throw IllegalArgumentException() }

        else -> throw IllegalArgumentException("无效的节气：$solarTerm")
    }
}