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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.example.myapplication.ui.theme.MyApplicationTheme

class Blacklist : ComponentActivity() {

    // In order for the two views to be updated, we need to modify the reference to the list
    // https://slack-chats.kotlinlang.org/t/506543/hi-how-to-update-composable-when-a-list-changes-by-rember-mu
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Find main activities, disable finding system apps
        val context = this
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val sharedPreferences = context.getSharedPreferences("AppTimeLimits", Context.MODE_PRIVATE)


        var resolvedInfoList: List<ResolveInfo> =
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
        var blacklistedApps: MutableList<ResolveInfo> =
            resolvedInfoList.filter { resolveInfo -> sharedPreferences.contains(resolveInfo.activityInfo.packageName) }
                .toMutableStateList()
        var nonBlacklistedApps: MutableList<ResolveInfo> =
            resolvedInfoList.filter { resolveInfo -> !sharedPreferences.contains(resolveInfo.activityInfo.packageName) }
                .toMutableStateList()

        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Content(
                        blacklistedApps,
                        nonBlacklistedApps
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
//@Preview
@Composable
fun Content(
    blacklistedApps: MutableList<ResolveInfo>,
    nonBlacklistedApps: MutableList<ResolveInfo>,
) {
    val context = LocalContext.current
    val pm = context.packageManager

    var chosenIdx by remember {
        mutableStateOf(-1)
    }
    var secondUsage by remember {
        mutableStateOf("")
    }
    var minuteUsage by remember {
        mutableStateOf("")
    }
    var hourUsage by remember {
        mutableStateOf("")
    }
    val secondChange: (String) -> Unit = { it ->
        secondUsage = it
    }
    val minuteChange: (String) -> Unit = { it ->
        minuteUsage = it
    }
    val hourChange: (String) -> Unit = { it ->
        hourUsage = it
    }

    // 2 additional items for the texts
    val idxList = (0..blacklistedApps.size + nonBlacklistedApps.size + 1).toList()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        itemsIndexed(idxList) { _, idx ->
            if (idx == 0) {
                Text(text = "Choose apps to unblacklist")
            } else if (idx <= blacklistedApps.size) {
                val item: ResolveInfo = blacklistedApps[idx - 1]
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    onClick = {
                        chosenIdx = idx
                    },
                    modifier = Modifier
                        .size(width = 240.dp, height = 30.dp),
                ) {
                    Row {
                        Image(
                            bitmap = getAppImageBitmap(pm, item),
                            contentDescription = "app icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = getAppName(pm, item),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(alignment = Alignment.CenterVertically)
                        )
                        //                    Text(text = item.resolvePackageName)
                    }
                }
            } else if (idx == blacklistedApps.size + 1) {
                Text(text = "Choose apps to blacklist")
            } else {
                val item = nonBlacklistedApps[idx - blacklistedApps.size - 2]
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    onClick = {
                        chosenIdx = idx
                    },
                    modifier = Modifier
                        .size(width = 240.dp, height = 30.dp),
                ) {
                    Row {
                        Image(
                            bitmap = getAppImageBitmap(pm, item),
                            contentDescription = "app icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = getAppName(pm, item),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(alignment = Alignment.CenterVertically)
                        )
//                    Text(text = item.resolvePackageName)
                    }
                }
            }
        }
    }

    if (chosenIdx >= 1 && chosenIdx <= blacklistedApps.size) {
        val resolvedInfoItem = blacklistedApps[chosenIdx - 1]
        val packageName = resolvedInfoItem.activityInfo.packageName
        val savedTime = loadTimeLimit(context, packageName)
        println("Package name: " + packageName)
        println("Saved Time: " + savedTime)

        Dialog(
            onDismissRequest = { chosenIdx = -1 }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(375.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = getAppImageBitmap(pm, resolvedInfoItem),
                        contentDescription = "Card image"
                    )
                    Text(
                        getAppName(pm, resolvedInfoItem),
                        modifier = Modifier.padding(all = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = { chosenIdx = -1 }
                        ) {
                            Text("Dismiss")
                        }
                        TextButton(
                            onClick = {
                                chosenIdx = -1
                                nonBlacklistedApps.add(resolvedInfoItem)
                                blacklistedApps.remove(resolvedInfoItem)
                            }
                        ) {
                            Text("Unblacklist")
                        }
                    }
                }
            }
        }
    }


    if (chosenIdx >= blacklistedApps.size + 1) {
        val resolvedInfoItem = nonBlacklistedApps[chosenIdx - blacklistedApps.size - 2]

        hourUsage = "00"
        minuteUsage = "00"
        secondUsage = "00"

        var hourError = false
        var minuteError = false
        var secondError = false

        fun hourValidation(s: String) {
            hourError = !(isInteger(s) && 0 <= s.toInt() && s.toInt() <= 23 && s.length <= 2)
        }

        fun minuteValidation(s: String) {
            minuteError = !(isInteger(s) && 0 <= s.toInt() && s.toInt() <= 59 && s.length <= 2)
        }

        fun secondValidation(s: String) {
            secondError = !(isInteger(s) && 0 <= s.toInt() && s.toInt() <= 59 && s.length <= 2)
        }

        Dialog(
            onDismissRequest = { chosenIdx = -1 }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(375.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = getAppImageBitmap(pm, resolvedInfoItem),
                        contentDescription = "Card image"
                    )
                    Text(
                        getAppName(pm, resolvedInfoItem),
                        modifier = Modifier.padding(all = 16.dp)
                    )
                    Text(
                        "Set daily time limit",
                        modifier = Modifier.padding(all = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            label = { Text(text = "Hour") },
                            value = hourUsage,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            onValueChange = {
                                hourChange(it)
                                hourValidation(it)
                            },
                            modifier = Modifier
                                .weight(.2f),
                            keyboardActions = KeyboardActions { hourValidation(hourUsage) },
//                                .background(Color.White),
                            isError = hourError,
                            trailingIcon = {
                                if (hourError)
                                    Icon(
                                        Icons.Filled.Warning,
                                        "error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                            },
                        )
                        OutlinedTextField(
                            label = { Text(text = "Minute") },
                            value = minuteUsage,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            onValueChange = {
                                minuteChange(it)
                                minuteValidation(it)
                            },
                            modifier = Modifier.weight(.2f),
                            keyboardActions = KeyboardActions { minuteValidation(minuteUsage) },
                            isError = minuteError,
                            trailingIcon = {
                                if (minuteError)
                                    Icon(
                                        Icons.Filled.Warning,
                                        "error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                            },
                        )
                        OutlinedTextField(
                            label = { Text(text = "Second") },
                            value = secondUsage,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            onValueChange = {
                                secondChange(it)
                                secondValidation(it)
                            },
                            modifier = Modifier.weight(.2f),
                            keyboardActions = KeyboardActions { secondValidation(secondUsage) },
                            isError = secondError,
                            trailingIcon = {
                                if (secondError)
                                    Icon(
                                        Icons.Filled.Warning,
                                        "error",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                            },
                        )
                    }
                    // TODO: Get usage statistics here from UsageService
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = { chosenIdx = -1 }
                        ) {
                            Text("Dismiss")
                        }
                        TextButton(
                            onClick = {
                                if (!hourError && !minuteError && !secondError) {
                                    val timeLimit = "$hourUsage:$minuteUsage:$secondUsage"
                                    saveTimeLimit(
                                        context,
                                        resolvedInfoItem.activityInfo.packageName,
                                        timeLimit
                                    )
                                    chosenIdx = -1
                                    blacklistedApps.add(resolvedInfoItem)
                                    nonBlacklistedApps.remove(resolvedInfoItem)
                                }
                            }
                        ) {
                            Text("Set usage")
                        }
                    }
                }
            }
        }
    }
}

fun getAppImageBitmap(pm: PackageManager, resolveInfo: ResolveInfo): ImageBitmap {
    return resolveInfo.loadIcon(pm).toBitmap().asImageBitmap()
}

fun getAppName(pm: PackageManager, resolveInfo: ResolveInfo): String {
    val resources = pm.getResourcesForApplication(resolveInfo.activityInfo.applicationInfo)
    if (resolveInfo.activityInfo.labelRes != 0) {
        return resources.getString(resolveInfo.activityInfo.labelRes)
    } else {
        return resolveInfo.activityInfo.applicationInfo.loadLabel(pm).toString()
    }
}

fun saveTimeLimit(context: Context, packageName: String, timeLimit: String) {
    val sharedPreferences = context.getSharedPreferences("AppTimeLimits", Context.MODE_PRIVATE)
    sharedPreferences.edit().apply {
        putString(packageName, timeLimit)
        apply()
    }
}

fun loadTimeLimit(context: Context, packageName: String): String {
    val sharedPreferences = context.getSharedPreferences("AppTimeLimits", Context.MODE_PRIVATE)
    return sharedPreferences.getString(packageName, "") ?: ""
}

fun isInteger(s: String): Boolean {
    return s.toIntOrNull() != null
}
