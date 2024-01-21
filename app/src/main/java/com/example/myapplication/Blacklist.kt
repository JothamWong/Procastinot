package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

class Blacklist : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        val context = this
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val sharedPreferences = context.getSharedPreferences("AppTimeLimits", Context.MODE_PRIVATE)
        val resolvedInfoList =
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
        val blacklistedApps =
            resolvedInfoList.filter { resolveInfo -> sharedPreferences.contains(resolveInfo.activityInfo.packageName) }
                .toMutableStateList()
        val nonBlacklistedApps =
            resolvedInfoList.filter { resolveInfo -> !sharedPreferences.contains(resolveInfo.activityInfo.packageName) }
                .toMutableStateList()

        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Content(blacklistedApps, nonBlacklistedApps)
                }
            }
        }
    }

    companion object {
        fun getAppImageBitmap(pm: PackageManager, resolveInfo: ResolveInfo): ImageBitmap {
            return resolveInfo.loadIcon(pm).toBitmap().asImageBitmap()
        }

        fun getAppName(pm: PackageManager, resolveInfo: ResolveInfo): String {
            val resources = pm.getResourcesForApplication(resolveInfo.activityInfo.applicationInfo)
            return if (resolveInfo.activityInfo.labelRes != 0) {
                resources.getString(resolveInfo.activityInfo.labelRes)
            } else {
                resolveInfo.activityInfo.applicationInfo.loadLabel(pm).toString()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Content(
    blacklistedApps: MutableList<ResolveInfo>,
    nonBlacklistedApps: MutableList<ResolveInfo>
) {
    val context = LocalContext.current
    val pm = context.packageManager

    val chosenIdx = remember { mutableIntStateOf(-1) }

    val idxList = (0 until blacklistedApps.size + nonBlacklistedApps.size + 2).toList()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(idxList) { _, idx ->
            when {

                idx == 0 -> if (blacklistedApps.size > 0) {
                    Text(
                        text = "Choose apps to unblacklist",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                idx <= blacklistedApps.size -> AppCard(blacklistedApps[idx - 1], pm, chosenIdx, idx)
                idx == blacklistedApps.size + 1 -> Text(
                    text = "Choose apps to blacklist",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    textAlign = TextAlign.Center
                )

                else -> AppCard(
                    nonBlacklistedApps[idx - blacklistedApps.size - 2],
                    pm,
                    chosenIdx,
                    idx
                )
            }
        }
    }

    if (chosenIdx.intValue >= 1 && chosenIdx.intValue <= blacklistedApps.size) {
        AppDialog(blacklistedApps, nonBlacklistedApps, chosenIdx, context, true)
    } else if (chosenIdx.intValue > blacklistedApps.size + 1) {
        AppDialog(blacklistedApps, nonBlacklistedApps, chosenIdx, context, false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCard(item: ResolveInfo, pm: PackageManager, chosenIdx: MutableState<Int>, idx: Int) {
    val context = LocalContext.current
    val timeLimit = loadTimeLimit(context, item.activityInfo.packageName)

    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = { chosenIdx.value = idx },
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                bitmap = getAppImageBitmap(pm, item),
                contentDescription = "app icon",
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp)
            )
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
                    .padding(10.dp)
            ) {
                Text(
                    text = getAppName(pm, item),
                    style = MaterialTheme.typography.bodyLarge
                )
                if (timeLimit != null) Text(
                    text = "Limit: $timeLimit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AppDialog(
    blacklistedApps: MutableList<ResolveInfo>,
    nonBlacklistedApps: MutableList<ResolveInfo>,
    chosenIdx: MutableState<Int>,
    context: Context,
    blacklisted: Boolean
) {
    val pm = context.packageManager
    val resolvedInfoItem =
        if (chosenIdx.value <= blacklistedApps.size) blacklistedApps[chosenIdx.value - 1] else nonBlacklistedApps[chosenIdx.value - blacklistedApps.size - 2]
    val packageName = resolvedInfoItem.activityInfo.packageName
    val savedTime = loadTimeLimit(context, packageName)
    var savedMinutes = savedTime?.inWholeMinutes?.toInt() ?: 0
    val savedHours = savedMinutes / 60
    savedMinutes %= 60

    val localHourUsage = remember { mutableStateOf(savedHours.toString()) }
    val localMinuteUsage = remember { mutableStateOf(savedMinutes.toString()) }

    Dialog(onDismissRequest = { chosenIdx.value = -1 }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    bitmap = getAppImageBitmap(pm, resolvedInfoItem),
                    contentDescription = "Card image",
                    modifier = Modifier.size(80.dp)
                )
                Text(getAppName(pm, resolvedInfoItem), style = MaterialTheme.typography.titleMedium)
                TimeInputFields(localHourUsage, localMinuteUsage)
                DialogButtons(
                    blacklistedApps,
                    nonBlacklistedApps,
                    resolvedInfoItem,
                    localHourUsage,
                    localMinuteUsage,
                    context,
                    chosenIdx,
                    blacklisted
                )
            }
        }
    }
}

@Composable
fun TimeInputFields(hourUsage: MutableState<String>, minuteUsage: MutableState<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedTextField(
            label = { Text(text = "Hour") },
            value = hourUsage.value,
            onValueChange = { hourUsage.value = it },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            label = { Text(text = "Minute") },
            value = minuteUsage.value,
            onValueChange = { minuteUsage.value = it },
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
    }
}


@Composable
fun DialogButtons(
    blacklistedApps: MutableList<ResolveInfo>,
    nonBlacklistedApps: MutableList<ResolveInfo>,
    resolvedInfoItem: ResolveInfo,
    hourUsage: MutableState<String>,
    minuteUsage: MutableState<String>,
    context: Context,
    chosenIdx: MutableState<Int>,
    blacklisted: Boolean
) {
    Column {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { chosenIdx.value = -1 },
                modifier = Modifier
                    .fillMaxWidth(0.45f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val hours = hourUsage.value.toInt()
                    val minutes = minuteUsage.value.toInt()
                    val totalMinutes = hours * 60 + minutes
                    val totalDuration = totalMinutes.toDuration(DurationUnit.MINUTES)
                    saveTimeLimit(context, resolvedInfoItem.activityInfo.packageName, totalDuration)
                    chosenIdx.value = -1
                    if (!blacklistedApps.contains(resolvedInfoItem)) {
                        blacklistedApps.add(resolvedInfoItem)
                        nonBlacklistedApps.remove(resolvedInfoItem)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Save Changes")
            }


        }
        if (blacklisted) {
            Button(
                onClick = {
                    chosenIdx.value = -1
                    removeTimeLimit(
                        context,
                        resolvedInfoItem.activityInfo.packageName
                    )
                    nonBlacklistedApps.add(resolvedInfoItem)
                    blacklistedApps.remove(resolvedInfoItem)
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Unblacklist")
            }
        }
    }

}

fun getAppImageBitmap(pm: PackageManager, resolveInfo: ResolveInfo): ImageBitmap {
    return resolveInfo.loadIcon(pm).toBitmap().asImageBitmap()
}

fun getAppName(pm: PackageManager, resolveInfo: ResolveInfo): String {
    val resources = pm.getResourcesForApplication(resolveInfo.activityInfo.applicationInfo)
    return if (resolveInfo.activityInfo.labelRes != 0) resources.getString(resolveInfo.activityInfo.labelRes)
    else resolveInfo.activityInfo.applicationInfo.loadLabel(pm).toString()
}

fun saveTimeLimit(context: Context, packageName: String, timeLimit: Duration) {
    val sharedPreferences = context.getSharedPreferences("AppTimeLimits", Context.MODE_PRIVATE)
    sharedPreferences.edit().apply {
        putString(packageName, timeLimit.toString())
        apply()
    }
    MyAccessibilityService.instance.updateTrackPackages(packageName, timeLimit.toJavaDuration())
}

fun loadTimeLimit(context: Context, packageName: String): Duration? {
    val sharedPreferences = context.getSharedPreferences("AppTimeLimits", Context.MODE_PRIVATE)
    val timeString = sharedPreferences.getString(packageName, "")!!
    return Duration.parseOrNull(timeString)
}

fun removeTimeLimit(context: Context, packageName: String) {
    val sharedPreferences = context.getSharedPreferences("AppTimeLimits", Context.MODE_PRIVATE)
    sharedPreferences.edit().apply {
        remove(packageName)
        apply()
    }
    MyAccessibilityService.instance.removeTrackPackages(packageName)
}

