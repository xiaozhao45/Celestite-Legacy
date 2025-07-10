package com.xiaozhao45.celestite

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.xiaozhao45.celestite.ui.theme.CelestiteTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

class Seletor : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CelestiteTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Main()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main() {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var calcResult by remember { mutableStateOf<String?>(null) }

    Column {
        TopAppBar(
            modifier = Modifier.padding(end = 8.dp),
            title = {
                Text("选择起局时间")
            },
            actions = {
//                Button(
//                    onClick = {  }
//
//                ) {
//                    Icon(
//                        imageVector = Icons.Outlined.Refresh,
//                        contentDescription = "重设" // 啊嘞，这个功能貌似忘记实现了。不过先给我受着。
//                    )
//                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        showDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "确定"
                        )
                        Text("确定")
                    }
                }
            }
        )
        val datePickerState = rememberDatePickerState(
            yearRange = IntRange(1901, 2098),
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePicker(
            state = datePickerState,
            showModeToggle = true,
            modifier = Modifier
        )

        val calendar = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ){
            TimeInput(
                state = timePickerState,
                modifier = Modifier.padding(top = 32.dp)
            )
        }
        if (showDialog) {
            CalcDialog(
                datePickerState = datePickerState,
                timePickerState = timePickerState,
                onDismiss = { showDialog = false },
                onResult = { result ->
                    calcResult = result
                }
            )
        }
        if (calcResult != null) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                Button(
                    onClick = {
                        var resultIntent = Intent()
                        resultIntent.putExtra("result", calcResult)
                        (context as? Activity)?.setResult(Activity.RESULT_OK, resultIntent)
                        val activity = context as? ComponentActivity
                        activity?.finish()
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("传回主页以展示")
                }
            }

        }


    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalcDialog(
    datePickerState: DatePickerState,
    timePickerState: TimePickerState,
    onDismiss: () -> Unit,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCalculating by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    // 奇门遁甲，启动！（划掉
    LaunchedEffect(Unit) {
        scope.launch {
            isCalculating = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                result = Calcing(
                    datePickerState = datePickerState,
                    timePickerState = timePickerState
                )
                onResult(result)
            }
            isCalculating = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "正在演算...",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isCalculating) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    // 显示计算结果
                    if (result.isNotEmpty()) {
                        Text(
                            text = "计算结果:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "下方组件内容可任意滑动",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(8.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = result,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                softWrap = false, // 关闭自动换行，这样长行会超出边界
                            )
                        }


                    }

                    Spacer(modifier = Modifier.padding(8.dp))
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(result))
                            Toast.makeText(context, "已复制到剪贴板!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("复制演算结果")
                    }
                    Spacer(modifier = Modifier.padding(8.dp))


                    OutlinedButton(
                        onClick = onDismiss,

                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("关闭")
                    }
                }
            }
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnsafeDynamicallyLoadedCode")
@OptIn(ExperimentalMaterial3Api::class)
suspend fun Calcing(
    datePickerState: DatePickerState,
    timePickerState: TimePickerState
): String = withContext(Dispatchers.IO) {
    // 从 DatePicker 获取年月日
    val selectedDate = datePickerState.selectedDateMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    } ?: LocalDate.now()

    // 从 TimePicker 获取时分
    val hour = timePickerState.hour
    val minute = timePickerState.minute

    val year = selectedDate.year  // yyyy (已经是4位数)
    val month = selectedDate.monthValue  // MM (1-12)
    val day = selectedDate.dayOfMonth  // dd (1-31)

    // JNI，启动！
    return@withContext QimenJNI.calcWithValue(
        year, month, day, hour, minute, 0, 0,
        style = 0
    )
}

class QimenJNI {
    companion object {
        init {
            System.loadLibrary("qimen")
        }

        external fun calc(argc: Int, argv: Array<String>): String

        // 修改这个函数以匹配原来 main.c 的参数格式，没错，核心程序是我改过的。。。
        fun calcWithValue(year: Int, month: Int, day: Int, hour: Int, min: Int, sec: Int, gameNum: Int, style: Int): String {
            val argv = arrayOf(
                "$year-$month-$day-$hour-$min-$sec",  // 时间参数
                "$gameNum",  // 局数
                "$style"     // 样式？我也忘了，貌似是起局方法，不过这里恒为0，意思是时家转盘+置润。
            )
            return calc(argv.size, argv)
        }
    }
}

