package com.xiaozhao45.celestite

import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.ui.platform.LocalContext
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiaozhao45.celestite.ui.theme.CelestiteTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.palette.graphics.Palette
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color

// 一些常量
private const val TAG = "CelestiteApp"
private const val TIME_PATTERN = "yyyy年MM月dd日HH时mm分ss秒"

// 九宫和六十甲子
data class SixtyCycleInfo(
    val year: String = "",
    val month: String = "",
    val day: String = "",
    val hour: String = "",
    val gregorian: String = ""
)

data class Palace(
    var name: String = "",
    var upStem: String? = null,
    var downStem: String? = null,
    var door: String? = null,
    var star: String? = null,
    var upMystery: String? = null,
    val active: Boolean = false,
    val death: Boolean = false
) {
    val code: Int
        get() = when {
            name.contains("乾") -> 6
            name.contains("坤") -> 2
            name.contains("震") -> 3
            name.contains("巽") -> 4
            name.contains("坎") -> 1
            name.contains("兑") -> 7
            name.contains("艮") -> 8
            name.contains("离") -> 9
            name.contains("中") -> 5
            else -> 0
        }
}

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val themeRepository = remember { ThemeRepository(context) }
            val themeMode by themeRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            // 主题切换动画的实现
            var isThemeChanging by remember { mutableStateOf(false) }
            var previousTheme by remember { mutableStateOf(themeMode) }

            LaunchedEffect(themeMode) {
                if (previousTheme != themeMode) {
                    isThemeChanging = true
                    kotlinx.coroutines.delay(300) // 动画持续时间在这里
                    isThemeChanging = false
                    previousTheme = themeMode
                }
            }

            CelestiteTheme(themeMode = themeMode) {
                AnimatedThemeContainer(
                    isChanging = isThemeChanging
                ) {
                    Scaffold {
                        MainSrceen(themeRepository = themeRepository, currentTheme = themeMode)
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedThemeContainer(
    isChanging: Boolean,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isChanging) 0.95f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "themeScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isChanging) 0.7f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "themeAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
            .alpha(alpha)
    ) {
        content()

        AnimatedVisibility(
            visible = isChanging,
            enter = fadeIn(
                animationSpec = tween(150)
            ),
            exit = fadeOut(
                animationSpec = tween(150)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    result: String? = null,
    year: Int? = null,
    month: Int? = null,
    day: Int? = null,
    hour: Int? = null,
    min: Int? = null,
    sec: Int? = null,
    launcher: ActivityResultLauncher<Intent>? = null
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        TopAppBar(
            title = { Text("奇门遁甲") },
            actions = {
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Outlined.Info, contentDescription = "Info")
                }
            }
        )

        if (showDialog) {
            InfoDialog(onDismiss = { showDialog = false })
        }

        Spacer(modifier = Modifier.height(16.dp))

        val timeInfo = when {
            result != null -> parseTimeFromResult(result)
            else -> getCurrentTimeInfo(year, month, day, hour, min, sec)
        }

        BasicInfoCard(
            timeInfo = timeInfo,
            sixtyCycle = getSixtyCycleString(year, month, day, hour, min, sec),
            launcher = launcher
        )

        Spacer(modifier = Modifier.height(16.dp))
        PalaceGrid(result, modifier = Modifier
            .weight(1f))
        Spacer(modifier = Modifier.height(16.dp))
        IntroCard()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun InfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("一些帮助信息") },
        text = {
            Text("Q:为什么中宫也会显示八神和九星？\nA:这就是底层程序的逻辑了，底层程序是用C语言硬算出来的。")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getCurrentTimeInfo(
    year: Int?, month: Int?, day: Int?, hour: Int?, min: Int?, sec: Int?
): SixtyCycleInfo {
    val currentTime = if (listOf(year, month, day, hour, min, sec).any { it == null }) {
        LocalDateTime.now()
    } else {
        LocalDateTime.of(year!!, month!!, day!!, hour!!, min!!, sec!!)
    }

    val formatter = DateTimeFormatter.ofPattern(TIME_PATTERN)
    val formattedTime = currentTime.format(formatter)

    return SixtyCycleInfo(gregorian = formattedTime)
}

private fun getSixtyCycleString(
    year: Int?, month: Int?, day: Int?, hour: Int?, min: Int?, sec: Int?
): String? {
    return if (listOf(year, month, day, hour, min, sec).none { it == null }) {
        SixtyCycleFormatted(year!!, month!!, day!!, hour!!, min!!, sec!!)
    } else null
}

// 解析result
private fun parseTimeFromResult(result: String?): SixtyCycleInfo {
    if (result.isNullOrEmpty()) return SixtyCycleInfo()

    return try {
        val gregorianTime = extractGregorianTime(result)
        val (stemLine, branchLine) = extractStemAndBranch(result)

        val stems = stemLine.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val branches = branchLine.split("\\s+".toRegex()).filter { it.isNotEmpty() }

        SixtyCycleInfo(
            year = if (stems.isNotEmpty() && branches.isNotEmpty()) "${stems[0]}${branches[0]}" else "",
            month = if (stems.size > 1 && branches.size > 1) "${stems[1]}${branches[1]}" else "",
            day = if (stems.size > 2 && branches.size > 2) "${stems[2]}${branches[2]}" else "",
            hour = if (stems.size > 3 && branches.size > 3) "${stems[3]}${branches[3]}" else "",
            gregorian = gregorianTime
        )
    } catch (e: Exception) {
        Log.e(TAG, "解析失败: ${e.message}，我要炸了。")
        SixtyCycleInfo()
    }
}

private fun extractGregorianTime(result: String): String {
    val gregorianRegex = """公元:(\d{4}年\d{2}月\d{2}日\d{2}时\d{2}分\d{2}秒)""".toRegex()
    return gregorianRegex.find(result)?.groupValues?.get(1) ?: ""
}

private fun extractStemAndBranch(result: String): Pair<String, String> {
    val lines = result.split("\n")

    for (i in lines.indices) {
        if (lines[i].startsWith("干支:")) {
            val stemLine = lines[i].substringAfter("干支:").trim()
            val branchLine = if (i + 1 < lines.size) {
                val branchData = lines[i + 1].trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
                branchData.take(4).joinToString(" ")
            } else ""
            return Pair(stemLine, branchLine)
        }
    }
    return Pair("", "")
}

@Composable
private fun BasicInfoCard(
    sixtyCycle: String? = null,
    timeInfo: SixtyCycleInfo,
    launcher: ActivityResultLauncher<Intent>? = null
) {
    val context = LocalContext.current

    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            launcher?.let {
                val intent = Intent(context, Seletor::class.java)
                it.launch(intent)
            }
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "基本信息",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            val displaySixtyCycle = if (timeInfo.year.isNotEmpty()) {
                "${timeInfo.year}年 ${timeInfo.month}月 ${timeInfo.day}日 ${timeInfo.hour}时"
            } else {
                sixtyCycle ?: ""
            }

            if (displaySixtyCycle.isNotEmpty()) {
                Text(
                    text = "干支时间：$displaySixtyCycle",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (timeInfo.gregorian.isNotEmpty()) {
                Text(
                    text = "公历时间：${timeInfo.gregorian}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// 太好了是九宫，我们有救了（先前程序还只有JSON输出）
@Composable
fun PalaceGrid(result: String? = null, modifier: Modifier = Modifier) {
    val palaceNames = remember {
        listOf(
            "巽4宫", "离9宫", "坤2宫",
            "震3宫", "中5宫", "兑7宫",
            "艮8宫", "坎1宫", "乾6宫"
        )
    }

    val parsedPalaces = remember(result) {
        result?.let { parseRawTextToPalaces(it) }
    }

    val hasData = parsedPalaces?.isNotEmpty() == true
    var showBlurOverlay by remember(result) { mutableStateOf(hasData) }

    // 模糊动画
    val blurRadius by animateFloatAsState(
        targetValue = if (showBlurOverlay) 40f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "blurRadius"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .blur(radius = blurRadius.dp), // 模糊应用在这里
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            items(palaceNames.size) { index ->
                PalaceCard(
                    palaceName = palaceNames[index],
                    parsedPalaces = parsedPalaces
                )
            }
        }

        if (hasData) {
            BlurOverlay(
                visible = showBlurOverlay,
                onDismiss = { showBlurOverlay = false }
            )
        }
    }
}

@Composable
private fun BlurOverlay(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val overlayAlpha by animateFloatAsState(
        targetValue = if (visible) 0.9f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "overlayAlpha"
    )

    val textScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "textScale"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = overlayAlpha * 0.7f),
                            MaterialTheme.colorScheme.surface.copy(alpha = overlayAlpha * 0.8f),
                            MaterialTheme.colorScheme.surface.copy(alpha = overlayAlpha * 0.7f)
                        )
                    )
                )
                .clickable { onDismiss() },

            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .scale(textScale)
                    .padding(horizontal = 32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.AllInclusive,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .alpha(overlayAlpha),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "点击此处显示",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(overlayAlpha)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "奇门遁甲排盘结果",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(overlayAlpha)
                    )
                }
            }
        }
    }
}

@Composable
private fun PalaceCard(
    palaceName: String,
    parsedPalaces: List<Palace>?
) {
    val code = getPalaceCode(palaceName)
    val palace = parsedPalaces?.find { it.name.contains(palaceName.take(1)) || it.code == code }

    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // 左下角宫名
            Text(
                text = palaceName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.BottomStart)
            )

            // 右下角天干地干
            PalaceStems(palace, Modifier.align(Alignment.BottomEnd))

            // 左上角空亡和驿马
            PalaceMarkers(palace, Modifier.align(Alignment.TopStart))

            // 中央八神、九星、八门
            PalaceCenterContent(palace, Modifier.align(Alignment.Center))

            // 调试信息
            if (palace == null && parsedPalaces != null) {
                Text(
                    text = "无数据",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun PalaceStems(palace: Palace?, modifier: Modifier) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier
    ) {
        palace?.upStem?.let { upStem ->
            Text(
                text = upStem,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End
            )
        }

        palace?.downStem?.let { downStem ->
            Text(
                text = downStem,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun PalaceMarkers(palace: Palace?, modifier: Modifier) {
    Column(
        verticalArrangement = Arrangement.Top,
        modifier = modifier
    ) {
        if (palace?.death == true) {
            Text(
                text = "○",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (palace?.active == true) {
            Text(
                text = "♞",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PalaceCenterContent(palace: Palace?, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .offset(y = (-8).dp)
    ) {
        palace?.upMystery?.let { upMystery ->
            Text(
                text = upMystery,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        palace?.star?.let { star ->
            Text(
                text = star,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        palace?.door?.let { door ->
            Text(
                text = door,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getPalaceCode(palaceName: String): Int = when(palaceName) {
    "乾6宫" -> 6
    "坤2宫" -> 2
    "震3宫" -> 3
    "巽4宫" -> 4
    "坎1宫" -> 1
    "兑7宫" -> 7
    "艮8宫" -> 8
    "离9宫" -> 9
    "中5宫" -> 5
    else -> 0
}

private fun parseRawTextToPalaces(rawText: String): List<Palace> {
    return try {
        val jsonString = extractJsonFromText(rawText) ?: return emptyList()

        if (!isValidJson(jsonString)) {
            Log.e(TAG, "提取的内容非有效JSON")
            return emptyList()
        }

        val gson = Gson()
        val listType = object : TypeToken<List<Palace>>() {}.type
        val palaces: List<Palace> = gson.fromJson(jsonString, listType)

        palaces.map { palace ->
            palace.copy(upStem = processUpStem(palace.name, palace.upStem))
        }
    } catch (e: JsonSyntaxException) {
        Log.e(TAG, "JSON解析语法错误", e)
        emptyList()
    } catch (e: Exception) {
        Log.e(TAG, "解析失败，我要炸了（虽然这里一般是Unreachable", e)
        emptyList()
    }
}

private fun extractJsonFromText(rawText: String): String? {
    val jsonPattern = Regex("""\[\s*\{[\s\S]*\}\s*\]""")
    return jsonPattern.find(rawText)?.value
}

private fun isValidJson(jsonString: String): Boolean {
    return try {
        JsonParser.parseString(jsonString)
        true
    } catch (e: JsonSyntaxException) {
        false
    }
}

private fun processUpStem(name: String, originalUpStem: String?): String? {
    if (originalUpStem.isNullOrEmpty()) return null

    if (name.contains("中")) return null

    return when {
        originalUpStem == "甲甲" -> null
        originalUpStem.startsWith("甲") && originalUpStem.length == 2 -> {
            originalUpStem.substring(1)
        }
        originalUpStem.length == 2 && !originalUpStem.contains("甲") -> originalUpStem
        else -> originalUpStem
    }
}

@Composable
private fun IntroCard() {
    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "注意",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "注意",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "切勿滥用奇门遁甲。",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    themeRepository: ThemeRepository,
    currentTheme: ThemeMode
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "设置",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 主题设置卡片
        Card(
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = "主题",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "主题设置",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 主题选择
                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    AnimatedThemeOption(
                        text = "浅色主题",
                        selected = currentTheme == ThemeMode.LIGHT,
                        onClick = {
                            scope.launch {
                                themeRepository.setThemeMode(ThemeMode.LIGHT)
                            }
                        }
                    )

                    AnimatedThemeOption(
                        text = "深色主题",
                        selected = currentTheme == ThemeMode.DARK,
                        onClick = {
                            scope.launch {
                                themeRepository.setThemeMode(ThemeMode.DARK)
                            }
                        }
                    )

                    AnimatedThemeOption(
                        text = "跟随系统",
                        selected = currentTheme == ThemeMode.SYSTEM,
                        onClick = {
                            scope.launch {
                                themeRepository.setThemeMode(ThemeMode.SYSTEM)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 其他设置项目还没想好。。。
        Card(
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "关于应用",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Celestite v1.0",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "基于时家转盘置润法奇门遁甲理论制作！",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AnimatedThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    // 选中时的动小小动画效果
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "optionScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "optionBackground"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.small
            )
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 单选按钮动画
        AnimatedRadioButton(selected = selected)

        Spacer(modifier = Modifier.width(12.dp))

        // 文字动画
        AnimatedVisibility(
            visible = true,
            enter = slideInHorizontally() + fadeIn(),
            exit = slideOutHorizontally() + fadeOut()
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun AnimatedRadioButton(selected: Boolean) {
    val innerCircleScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "radioInnerScale"
    )

    RadioButton(
        selected = selected,
        onClick = null,
        modifier = Modifier.scale(
            scaleX = if (selected) 1.1f else 1f,
            scaleY = if (selected) 1.1f else 1f
        )
    )
}

@Composable
private fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainSrceen(
    themeRepository: ThemeRepository? = null,
    currentTheme: ThemeMode = ThemeMode.SYSTEM
) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    var selectorResult by remember { mutableStateOf<String?>(null) }

    val items = listOf("窥探天道", "敕令吾身") // 好几把中二。。。
    val selectedItemIcons = listOf(Icons.Filled.AllInclusive, Icons.Filled.Settings)
    val unselectedIcons = listOf(Icons.Outlined.AllInclusive, Icons.Outlined.Settings)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            selectorResult = activityResult.data?.getStringExtra("result")
            Log.d(TAG, "收到Selector结果: $selectorResult")
        }
    }

    var time = java.time.LocalTime.now()
    var today = LocalDate.now()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = selectedItem,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth ->
                            if (targetState > initialState) fullWidth else -fullWidth
                        },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300)) togetherWith
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth ->
                                    if (targetState > initialState) -fullWidth else fullWidth
                                },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                },
                label = "pageTransition"
            ) { targetSelectedItem ->
                when (targetSelectedItem) {
                    0 -> ForecastScreen(
                        result = selectorResult,
                        launcher = launcher,
                        year = today.year,
                        month = today.monthValue,
                        day = today.dayOfMonth,
                        hour = time.hour,
                        min = time.minute,
                        sec = time.second
                    )
                    1 -> themeRepository?.let { repo ->
                        SettingsScreen(themeRepository = repo, currentTheme = currentTheme)
                    } ?: run {
                        // 预览模式的默认设置页面
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "设置页面",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        NavigationBar {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            if (selectedItem == index) selectedItemIcons[index] else unselectedIcons[index],
                            contentDescription = item
                        )
                    },
                    label = { Text(item) },
                    selected = selectedItem == index,
                    onClick = { selectedItem = index }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MainPreview() {
    CelestiteTheme {
        // 怎么还翻Preview，不要乱看起出来的局。
        val result = """
            ===========================================
            感谢奇门遁甲排盘程序CQM的开发者：taynpg (Gitee名称)
            本排盘程序使用了此程序作为内核。公元:2025年07月07日15时00分00秒 遁甲排盘cqm
            ===========================================
            农历:二零二五年 六月十三日 申时
            干支:乙 癸 丁 戊
                 巳 未 丑 申             (转盘超接置润)
            ===========================================
            [
              {"name": "坎1宫", "code": 1, "upStem": "甲乙", "downStem": "癸", "door": "开门", "star": "天柱", "upMystery": "九天", "active": false, "death": false},
              {"name": "坤2宫", "code": 2, "upStem": "庆", "downStem": "壬", "door": "景门", "star": "天辅", "upMystery": "太阴", "active": false, "death": false},
              {"name": "震3宫", "code": 3, "upStem": "庚", "downStem": "辛", "door": "伤门", "star": "天任", "upMystery": "白虎", "active": false, "death": true},
              {"name": "巽4宫", "code": 4, "upStem": "辛", "downStem": "丁", "door": "杜门", "star": "天冲", "upMystery": "六合", "active": false, "death": false},
              {"name": "中5宫", "code": 5, "upStem": "", "downStem": "己", "door": "", "star": "", "upMystery": "", "active": false, "death": false},
              {"name": "乾6宫", "code": 6, "upStem": "壬己", "downStem": "戊", "door": "惊门", "star": "芮禽", "upMystery": "值符", "active": false, "death": false},
              {"name": "兑7宫", "code": 7, "upStem": "丁", "downStem": "乙", "door": "死门", "star": "天英", "upMystery": "腾蛇", "active": false, "death": false},
              {"name": "艮8宫", "code": 8, "upStem": "戊", "downStem": "丙", "door": "休门", "star": "天心", "upMystery": "九地", "active": true, "death": true},
              {"name": "离9宫", "code": 9, "upStem": "癸", "downStem": "辛", "door": "生门", "star": "天蓬", "upMystery": "玄武", "active": false, "death": true}
            ]
        """.trimIndent()

        ForecastScreen(result = result)
    }
}